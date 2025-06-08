package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.Query
import com.k5.omsetku.model.Sale
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class SaleRepository {
    private fun getSalesCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("sales")
    }

    suspend fun addSale(sale: Sale): Result<Sale> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val documentReference = getSalesCollection(uid).add(sale).await()
            sale.saleId = documentReference.id
            Result.success(sale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSales(): Result<List<Sale>> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val querySnapshot = getSalesCollection(uid)
                .orderBy("saleDate", Query.Direction.DESCENDING)
                .get().await()
            val sales = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Sale::class.java)?.apply { this.saleId = document.id }
            }
            Result.success(sales)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

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
                    Result.failure(NoSuchElementException("Failed to convert sale!"))
                }
            } else {
                Result.failure(NoSuchElementException("Sale not found!"))
            }
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
        val saleDetailsCollection = saleRef.collection("sale_details")

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