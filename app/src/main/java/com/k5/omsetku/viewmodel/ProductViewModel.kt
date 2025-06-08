package com.k5.omsetku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k5.omsetku.model.Product
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class ProductViewModel: ViewModel() {
    private val productRepo = ProductRepository()

    // MutableLiveData untuk mengelola state data (loading, success, error)
    private val _products =  MutableLiveData<LoadState<List<Product>>>()
    val products: LiveData<LoadState<List<Product>>> = _products

    // Variabel untuk menandakan apakah data sudah dimuat setidaknya sekali
    private var isDataLoaded = false

    init {
        loadProducts()
    }

    fun loadProducts(forceRefresh: Boolean = false) {
        // Jika data sudah dimuat dan tidak ada paksaan refresh, jangan muat ulang
        if (isDataLoaded && !forceRefresh && _products.value is LoadState.Success) {
            return
        }

        _products.value = LoadState.Loading

        viewModelScope.launch {
            val result = productRepo.getProducts()
            result.onSuccess { productList ->
                _products.value = LoadState.Success(productList)
                isDataLoaded = true // Tandai bahwa data sudah dimuat
            }.onFailure { e ->
                _products.value = LoadState.Error(e.message ?: "Unknown error")
                isDataLoaded = false // Reset isDataLoaded jika ada error agar bisa coba lagi
            }
        }
    }

    // Fungsi untuk memuat ulang data secara paksa (misalnya, setelah edit/hapus)
    fun refreshProducts() {
        loadProducts(true)
    }

    suspend fun addProduct (product: Product): Result<Product> {
        val result = productRepo.addProduct(product)
        if (result.isSuccess) {
            refreshProducts()
        }
        return result
    }

    suspend fun updateProduct (productId: String, updatedProduct: Map<String, Any>): Result<Unit> {
        val result = productRepo.updateProduct(productId, updatedProduct)
        if (result.isSuccess) {
            refreshProducts()
        }
        return result
    }

    suspend fun deleteProduct(productId: String): Result<Unit> {
        val result = productRepo.deleteProduct(productId)
        if (result.isSuccess) {
            refreshProducts()
        }
        return result
    }
}