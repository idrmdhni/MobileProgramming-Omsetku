package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class SaleDetailRepository {
    private val productRepository = ProductRepository()

    private fun getSalesDetailsCollection(uid: String, saleId: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("sales")
            .document(saleId)
            .collection("sales_details")
    }

    // CREATE
    suspend fun addSaleDetail(saleId: String, saleDetail: SaleDetail): Result<SaleDetail> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login."))
        }

        return try {
            // Validasi untuk memastikan produk yang diacu valid dan milik pengguna ini
            val productResult = productRepository.getProductById(saleDetail.productId)
            if (productResult.isFailure) {
                return Result.failure(productResult.exceptionOrNull() ?: Exception("Produk tidak ditemukan atau error validasi."))
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
            return Result.failure(IllegalStateException("Pengguna belum login."))
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
            return Result.failure(IllegalStateException("Pengguna belum login."))
        }

        return try {
            val documentSnapshot = getSalesDetailsCollection(uid, saleId).document(saleDetailId).get().await()
            if (documentSnapshot.exists()) {
                val detail = documentSnapshot.toObject(SaleDetail::class.java)?.apply { this.saleDetailId = documentSnapshot.id }
                if (detail != null) {
                    Result.success(detail)
                } else {
                    Result.failure(NoSuchElementException("Gagal mengonversi detail penjualan."))
                }
            } else {
                Result.failure(NoSuchElementException("Detail penjualan tidak ditemukan."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE
    suspend fun updateSaleDetail(saleId: String, saleDetailId: String, updatedSaleDetailData: Map<String, Any>): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login."))
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
            return Result.failure(IllegalStateException("Pengguna belum login."))
        }

        return try {
            getSalesDetailsCollection(uid, saleId).document(detailId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}