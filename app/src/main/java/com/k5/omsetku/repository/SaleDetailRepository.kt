package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class SaleDetailRepository {
    private val productRepo: ProductRepository by lazy { ProductRepository() }

    private fun getSalesDetailsCollection(uid: String, saleId: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("sales")
            .document(saleId)
            .collection("sales_details")
    }

    // Cek apakah ada produk dalam SaleDetail
    suspend fun hasProductInSaleDetails(productId: String): Result<Boolean> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val salesCollection = FirebaseUtils.db.collection("users")
                .document(uid)
                .collection("sales")
            // Cek setidaknya apakah ada 1 produk dalam SaleDetail
            val salesSnapshot = salesCollection.get().await()
            for (saleDocument in salesSnapshot.documents) {
                val saleId = saleDocument.id
                val salesDetailsCollection = getSalesDetailsCollection(uid, saleId)
                val querySnapshot = salesDetailsCollection
                    .whereEqualTo("productId", productId)
                    .limit(1)
                    .get()
                    .await()

                if (!querySnapshot.isEmpty) {
                    return Result.success(true) // Produk ditemukan di salah satu SaleDetail
                }
            }
            Result.success(false) // Produk tidak ditemukan di SaleDetail manapun
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // CREATE
    suspend fun addSaleDetail(saleId: String, saleDetail: SaleDetail): Result<SaleDetail> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            // Validasi untuk memastikan produk yang diacu valid dan milik pengguna ini
            val productResult = productRepo.getProductById(saleDetail.productId)
            if (productResult.isFailure) {
                return Result.failure(productResult.exceptionOrNull() ?: Exception("Product not found or validation error"))
            }

            val documentReference = getSalesDetailsCollection(uid, saleId).add(saleDetail).await()
            saleDetail.saleDetailId = documentReference.id
            Result.success(saleDetail)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ
    suspend fun getSalesDetails(saleId: String): Result<List<SaleDetail>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val querySnapshot = getSalesDetailsCollection(uid, saleId).get().await()
            val saleDetails = querySnapshot.documents.mapNotNull { document ->
                document.toObject(SaleDetail::class.java)?.apply { this.saleDetailId = document.id }
            }
            Result.success(saleDetails)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ (Mendapatkan satu data berdasarkan ID)
    suspend fun getSaleDetailById(saleId: String, saleDetailId: String): Result<SaleDetail> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val documentSnapshot = getSalesDetailsCollection(uid, saleId).document(saleDetailId).get().await()
            if (documentSnapshot.exists()) {
                val detail = documentSnapshot.toObject(SaleDetail::class.java)?.apply { this.saleDetailId = documentSnapshot.id }
                if (detail != null) {
                    Result.success(detail)
                } else {
                    Result.failure(NoSuchElementException("Failed to convert sale detail!"))
                }
            } else {
                Result.failure(NoSuchElementException("Sale detail not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE
    suspend fun updateSaleDetail(saleId: String, saleDetailId: String, updatedSaleDetailData: Map<String, Any>): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            getSalesDetailsCollection(uid, saleId).document(saleDetailId).update(updatedSaleDetailData).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE
    suspend fun deleteSaleDetail(saleId: String, detailId: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            getSalesDetailsCollection(uid, saleId).document(detailId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}