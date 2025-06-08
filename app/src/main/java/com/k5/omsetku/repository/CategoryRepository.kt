package com.k5.omsetku.repository

import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.Category
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class CategoryRepository {
    private fun getCategoriesCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("categories")
    }

    suspend fun addCategory(name: String): Result<Category> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login!"))
        }

        val newCategory = Category(categoryName = name)

        return try {
            val documentReference = getCategoriesCollection(uid).add(newCategory).await()
            newCategory.categoryId = documentReference.id
            Result.success(newCategory)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCategories(): Result<List<Category>> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login!"))
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

    suspend fun getCategoryById(id: String): Result<Category> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login!"))
        }

        return try {
            val documentSnapshot = getCategoriesCollection(uid).document(id).get().await()
            if (documentSnapshot.exists()) {
                val category = documentSnapshot.toObject(Category::class.java)?.apply { this.categoryId = documentSnapshot.id }
                if (category != null) {
                    Result.success(category)
                } else {
                    Result.failure(NoSuchElementException("Gagal mengonversi kategori"))
                }
            } else {
                Result.failure(NoSuchElementException("Kategori tidak ditemukan!"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategory(id: String, newName: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login!"))
        }

        return try {
            val categoryRef = getCategoriesCollection(uid).document(id)
            categoryRef.update("categoryName", newName).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(id: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login!"))
        }

        return try {
            getCategoriesCollection(uid).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}