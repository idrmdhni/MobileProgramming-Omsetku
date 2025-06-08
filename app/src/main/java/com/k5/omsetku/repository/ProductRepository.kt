package com.k5.omsetku.repository

import android.widget.Toast
import com.google.firebase.firestore.CollectionReference
import com.k5.omsetku.model.Category
import com.k5.omsetku.model.Product
import com.k5.omsetku.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private fun getProductCollection(uid: String): CollectionReference {
        return FirebaseUtils.db.collection("users")
            .document(uid)
            .collection("products")
    }

    suspend fun addProduct(name: String, stock: Int, price: Long, desc: String, catId: String): Result<Product> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login"))
        }

        val newProduct = Product(productName = name, productStock = stock, productPrice = price, productDescription = desc, categoryId = catId)

        return try {
            val documentRef = getProductCollection(uid).add(newProduct).await()
            newProduct.productId = documentRef.id
            Result.success(newProduct)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(): Result<List<Product>> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login"))
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

    suspend fun getProductById(id: String): Result<Product> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login"))
        }

        return try {
            val documentSnapshot = getProductCollection(uid).document(id).get().await()
            if (documentSnapshot.exists()) {
                val document = documentSnapshot.toObject(Product::class.java)?.apply { this.productId = documentSnapshot.id }

                if (document != null) {
                    Result.success(document)
                } else {
                    Result.failure(NoSuchElementException("Gagal mengonversi produk"))
                }
            } else {
                Result.failure(NoSuchElementException("Produk tidak ditemukan"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProduct(id: String, newName: String, newStock: Int, newPrice: Long, newDesc: String, newCatId: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login"))
        }

        return try {
            val documentRef = getProductCollection(uid).document(id)
            documentRef.update("productName", newName).await()
            documentRef.update("productStock", newStock).await()
            documentRef.update("productPrice", newPrice).await()
            documentRef.update("productDescription", newDesc).await()
            documentRef.update("categoryId", newCatId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteProduct(id: String): Result<Unit> {
        val uid = FirebaseUtils.getCurrentUserId()

        if (uid == null) {
            return Result.failure(IllegalStateException("Pengguna belum login"))
        }

        return try {
            getProductCollection(uid).document(id).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}