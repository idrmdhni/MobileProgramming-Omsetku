package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.Product
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val categoryRepo: CategoryRepository by lazy { CategoryRepository() }
    private val saleDetailRepo: SaleDetailRepository by lazy { SaleDetailRepository() }

    private fun getProductsCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("products")
    }

    // Cek apakah ada produk yang menggunakan kategori dicari
    suspend fun hasProductsInCategory(categoryId: String): Result<Boolean> {
        val uid = FirebaseUtils.getCurrentUserId()
        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            // Cek setidaknya apakah ada 1 produk dari kategori yang diberikan
            val querySnapshot = getProductsCollection(uid)
                .whereEqualTo("categoryId", categoryId)
                .limit(1)
                .get()
                .await()

            Result.success(!querySnapshot.isEmpty) // Terdapat kategori yang sedang digunakan
        } catch (e: Exception) {
            Result.failure(e) // Tidak ada kategori yang digunakan
        }
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
                return Result.failure(categoryResult.exceptionOrNull() ?: Exception("Category not found or validation error"))
            }

            val documentRef = getProductsCollection(uid).add(product).await()
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
            val querySnapshot = getProductsCollection(uid).get().await()
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
            val documentSnapshot = getProductsCollection(uid).document(id).get().await()
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
            getProductsCollection(uid).document(productId).update(updatedProduct).await()
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
            // Periksa apakah produk digunakan dalam SaleDetail
            val hasProductInSaleDetailsResult = saleDetailRepo.hasProductInSaleDetails(productId)

            if (hasProductInSaleDetailsResult.isSuccess && hasProductInSaleDetailsResult.getOrNull() == true) {
                return Result.failure(IllegalStateException("The product cannot be deleted because it is already recorded in the sales details!"))
            } else if (hasProductInSaleDetailsResult.isFailure) {
                return Result.failure(hasProductInSaleDetailsResult.exceptionOrNull() ?: Exception("Failed to check product dependability"))
            }

            getProductsCollection(uid).document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}