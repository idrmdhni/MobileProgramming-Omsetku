package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.Category
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    private val productRepo: ProductRepository by lazy { ProductRepository() }

    private fun getCategoriesCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("categories")
    }

    // CREATE
    suspend fun addCategory(categoryName: String): Result<Category> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        val newCategory = Category(categoryName = categoryName)

        return try {
            val documentReference = getCategoriesCollection(uid).add(newCategory).await()
            newCategory.categoryId = documentReference.id
            Result.success(newCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ
    suspend fun getCategories(): Result<List<Category>> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val querySnapshot = getCategoriesCollection(uid).get().await()
            val categories = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Category::class.java)?.apply { this.categoryId = document.id }
            }
            Result.success(categories)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // READ (Mendapatkan satu data berdasarkan ID)
    suspend fun getCategoryById(categoryId: String): Result<Category> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val documentSnapshot = getCategoriesCollection(uid).document(categoryId).get().await()
            if (documentSnapshot.exists()) {
                val category = documentSnapshot.toObject(Category::class.java)?.apply { this.categoryId = documentSnapshot.id }
                if (category != null) {
                    Result.success(category)
                } else {
                    Result.failure(NoSuchElementException("Failed to convert category"))
                }
            } else {
                Result.failure(NoSuchElementException("Category not found!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // UPDATE
    suspend fun updateCategory(categoryId: String, newName: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            val categoryRef = getCategoriesCollection(uid).document(categoryId)
            categoryRef.update("categoryName", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // DELETE
    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("User is not logged in!"))
        }

        return try {
            // Periksa apakah ada produk yang masih menggunakan kategori ini
            val hasProductsResult = productRepo.hasProductsInCategory(categoryId)
            if (hasProductsResult.isSuccess && hasProductsResult.getOrNull() == true) {
                return Result.failure(IllegalStateException("The category cannot be deleted because it is still in use by the product!"))
            } else if (hasProductsResult.isFailure) {
                return Result.failure(hasProductsResult.exceptionOrNull() ?: Exception("Failed to check category dependency"))
            }

            getCategoriesCollection(uid).document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}