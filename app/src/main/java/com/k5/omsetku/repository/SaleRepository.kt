package com.k5.omsetku.repository

import android.icu.util.Calendar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.k5.omsetku.model.Product
import com.k5.omsetku.model.Sale
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await
import com.google.firebase.Timestamp

class SaleRepository {
    private val productRepo: ProductRepository by lazy { ProductRepository() }

    private fun getSalesCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("sales")
    }

    // CREATE
    suspend fun addSale(sale: Sale): Result<Sale> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        var saleToSave = sale

        sale.purchaseDate?.toDate()?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            val calculatedMonth = calendar.get(Calendar.MONTH) + 1
            val calculatedYear = calendar.get(Calendar.YEAR)

            // Buat salinan (copy) dari objek Sale dengan nilai month dan year yang diperbarui
            saleToSave = sale.copy(month = calculatedMonth, year = calculatedYear)
        }

        return try {
            val documentReference = getSalesCollection(uid).add(saleToSave).await()
            saleToSave.saleId = documentReference.id
            Result.success(saleToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CREATE
    suspend fun addSaleBatch(sale: Sale, saleDetailList: List<SaleDetail>): Result<Sale> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        var saleToSave = sale

        sale.purchaseDate?.toDate()?.let {
            val calendar = Calendar.getInstance().apply { time = it }
            val calculatedMonth = calendar.get(Calendar.MONTH) + 1
            val calculatedYear = calendar.get(Calendar.YEAR)

            // Buat salinan (copy) dari objek Sale dengan nilai month dan year yang diperbarui
            saleToSave = sale.copy(month = calculatedMonth, year = calculatedYear)
        }

        // Ambil semua produk yang terlibat dalam transaksi ini
        val productIds = saleDetailList.map { it.productId }.distinct()
        val productsToUpdate = mutableMapOf<String, Product>()

        for (productId in productIds) {
            val productResult = productRepo.getProductById(productId)
            if (productResult.isFailure) {
                return Result.failure(productResult.exceptionOrNull() ?: Exception("Product with ID $productId not found or failed to fetch."))
            }
            val product = productResult.getOrThrow()
            productsToUpdate[productId] = product
        }

        // Lakukan validasi stok untuk setiap saleDetail
        for (saleDetail in saleDetailList) {
            val product = productsToUpdate[saleDetail.productId]
            if (product == null) {
                return Result.failure(NoSuchElementException("Product with ID ${saleDetail.productId} not found during stock validation."))
            }

            if (saleDetail.quantity > product.productStock) {
                return Result.failure(
                    IllegalArgumentException("Insufficient stock for product '${product.productName}'. Available: ${product.productStock}, Requested: ${saleDetail.quantity}")
                )
            }
            // Kurangi stok di objek produk yang ada di memory untuk perhitungan batch
            product.productStock -= saleDetail.quantity
        }

        val batch = FirebaseUtils.db.batch()
        val saleDocRef = getSalesCollection(uid).document()
        batch.set(saleDocRef, saleToSave)

        for (saleDetail in saleDetailList) {
            val saleDetailDocRef = saleDocRef.collection("sales_details").document()
            batch.set(saleDetailDocRef, saleDetail)

            // Tambahkan operasi pengurangan stok ke batch
            val product = productsToUpdate[saleDetail.productId]
            if (product != null) {
                val productDocRef = FirebaseUtils.db.collection("users")
                    .document(uid)
                    .collection("products")
                    .document(saleDetail.productId)

                batch.update(productDocRef, "productStock", product.productStock)
            }
        }

        return try {
            batch.commit().await()
            Result.success(saleToSave)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ
    suspend fun getSales(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val querySnapshot = getSalesCollection(uid)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .get().await()
            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ (Mendapatkan satu data berdasarkan ID)
    suspend fun getSaleById(saleId: String): Result<Sale> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val documentSnapshot = getSalesCollection(uid).document(saleId).get().await()
            if (documentSnapshot.exists()) {
                val sale = documentSnapshot.toObject(Sale::class.java)?.apply { this.saleId = documentSnapshot.id }
                if (sale != null) {
                    Result.success(sale)
                } else {
                    Result.failure(NoSuchElementException("Failed to convert sale"))
                }
            } else {
                Result.failure(NoSuchElementException("Sale not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSalesByMonthAndYear(month: Int? = null, year: Int? = null): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        // Jika tidak ada filter yang diberikan sama sekali, kembalikan error.
        if (month == null && year == null) {
            return Result.failure(IllegalArgumentException("Setidaknya bulan atau tahun harus disediakan untuk filter."))
        }

        return try {
            var query: Query = getSalesCollection(uid)

            // Terapkan filter berdasarkan tahun jika ada
            if (year != null) {
                query = query.whereEqualTo("year", year)
            }

            // Terapkan filter berdasarkan bulan jika ada
            if (month != null) {
                query = query.whereEqualTo("month", month)
            }

            // Tambahkan pengurutan
            query = query.orderBy("purchaseDate", Query.Direction.DESCENDING)

            val querySnapshot = query.get().await()
            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Mengambil penjualan untuk hari ini
    suspend fun getSalesToday(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val calendarStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calendarEnd = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startTimestamp = Timestamp(calendarStart.time)
            val endTimestamp = Timestamp(calendarEnd.time)

            val querySnapshot = getSalesCollection(uid)
                .whereGreaterThanOrEqualTo("purchaseDate", startTimestamp)
                .whereLessThanOrEqualTo("purchaseDate", endTimestamp)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .get().await()

            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Mengambil penjualan untuk minggu ini (dari hari Minggu sampai Sabtu)
    suspend fun getSalesThisWeek(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val calendarStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek) // Atur ke hari pertama minggu (biasanya Minggu)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calendarEnd = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, firstDayOfWeek) // Atur ke hari pertama minggu
                add(Calendar.WEEK_OF_YEAR, 1) // Tambahkan satu minggu
                add(Calendar.MILLISECOND, -1) // Kurangi 1ms untuk akhir minggu sebelumnya
            }

            val startTimestamp = Timestamp(calendarStart.time)
            val endTimestamp = Timestamp(calendarEnd.time)

            val querySnapshot = getSalesCollection(uid)
                .whereGreaterThanOrEqualTo("purchaseDate", startTimestamp)
                .whereLessThanOrEqualTo("purchaseDate", endTimestamp)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .get().await()

            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Mengambil penjualan untuk bulan ini
    suspend fun getSalesThisMonth(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val calendarStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calendarEnd = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startTimestamp = Timestamp(calendarStart.time)
            val endTimestamp = Timestamp(calendarEnd.time)

            val querySnapshot = getSalesCollection(uid)
                .whereGreaterThanOrEqualTo("purchaseDate", startTimestamp)
                .whereLessThanOrEqualTo("purchaseDate", endTimestamp)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .get().await()

            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    // Mengambil penjualan untuk tahun ini
    suspend fun getSalesThisYear(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val calendarStart = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val calendarEnd = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_YEAR, getActualMaximum(Calendar.DAY_OF_YEAR))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            val startTimestamp = Timestamp(calendarStart.time)
            val endTimestamp = Timestamp(calendarEnd.time)

            val querySnapshot = getSalesCollection(uid)
                .whereGreaterThanOrEqualTo("purchaseDate", startTimestamp)
                .whereLessThanOrEqualTo("purchaseDate", endTimestamp)
                .orderBy("purchaseDate", Query.Direction.DESCENDING)
                .get().await()

            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE
    suspend fun updateSale(saleId: String, updatedSaleData: Map<String, Any>): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            getSalesCollection(uid).document(saleId).update(updatedSaleData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE (dengan penghapusan subcollection detail)
    suspend fun deleteSale(saleId: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        val saleRef = getSalesCollection(uid).document(saleId)
        val saleDetailsCollection = saleRef.collection("sales_details")

        return try {
            val querySnapshot = saleDetailsCollection.get().await()
            val batch = FirebaseUtils.db.batch()
            for (document in querySnapshot.documents) {
                batch.delete(document.reference)
            }
            batch.delete(saleRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}