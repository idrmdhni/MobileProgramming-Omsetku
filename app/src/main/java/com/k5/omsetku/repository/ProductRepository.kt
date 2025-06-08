package com.k5.omsetku.repository

import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val categoryRepo = CategoryRepository()

    private fun getProductCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("products")
    }

    // CREATE
    suspend fun addProduct(product: Product): Result<Product> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val categoryResult = categoryRepo.getCategoryById(product.categoryId)
            if (categoryResult.isFailure) {
                return Result.failure(categoryResult.exceptionOrNull() ?: Exception("Category not found or validation error!"))
            }

            val documentRef = getProductCollection(uid).add(product).await()
            product.productId = documentRef.id
            Result.success(product)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ
    suspend fun getProducts(): Result<List<Product>> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val querySnapshot = getProductCollection(uid).get().await()
            val products = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Product::class.java)?.apply { this.productId = document.id }
            }
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ (Mendapatkan satu data berdasarkan ID)
    suspend fun getProductById(id: String): Result<Product> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val documentSnapshot = getProductCollection(uid).document(id).get().await()
            if (documentSnapshot.exists()) {
                val document = documentSnapshot.toObject(Product::class.java)?.apply { this.productId = documentSnapshot.id }

                if (document != null) {
                    Result.success(document)
                } else {
                    Result.failure(NoSuchElementException("Failed to convert product!"))
                }
            } else {
                Result.failure(NoSuchElementException("Product not found!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE
    suspend fun updateProduct(productId: String, updatedProduct: Map<String, Any>): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            getProductCollection(uid).document(productId).update(updatedProduct).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE
    suspend fun deleteProduct(productId: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            getProductCollection(uid).document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}