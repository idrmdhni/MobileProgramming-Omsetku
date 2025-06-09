package com.k5.omsetku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k5.omsetku.model.Product
import com.k5.omsetku.model.Sale
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.repository.SaleRepository
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class SaleViewModel: ViewModel() {
    private val saleRepo = SaleRepository()

    // MutableLiveData untuk mengelola state data (loading, success, error)
    private val _sales =  MutableLiveData<LoadState<List<Sale>>>()
    val sales: LiveData<LoadState<List<Sale>>> = _sales

    // Variabel untuk menandakan apakah data sudah dimuat setidaknya sekali
    private var isDataLoaded = false

    init {
        loadSales()
    }

    fun loadSales(forceRefresh: Boolean = false) {
        // Jika data sudah dimuat dan tidak ada paksaan refresh, jangan muat ulang
        if (isDataLoaded && !forceRefresh && _sales.value is LoadState.Success) {
            return
        }

        _sales.value = LoadState.Loading

        viewModelScope.launch {
            val result = saleRepo.getSales()
            result.onSuccess { saleList ->
                _sales.value = LoadState.Success(saleList)
                isDataLoaded = true // Tandai bahwa data sudah dimuat
            }.onFailure { e ->
                _sales.value = LoadState.Error(e.message ?: "Unknown error")
                isDataLoaded = false // Reset isDataLoaded jika ada error agar bisa coba lagi
            }
        }
    }

    // Fungsi untuk memuat ulang data secara paksa (misalnya, setelah edit/hapus)
    fun refreshSales() {
        loadSales(true)
    }

    suspend fun addSale (sale: Sale): Result<Sale> {
        val result = saleRepo.addSale(sale)
        if (result.isSuccess) {
            refreshSales()
        }
        return result
    }

    suspend fun addSaleBatch (sale: Sale, saleDetailList: List<SaleDetail>): Result<Sale> {
        val result = saleRepo.addSaleBatch(sale, saleDetailList)
        if (result.isSuccess) {
            refreshSales()
        }
        return result
    }

    suspend fun updateSale (saleId: String, updatedSale: Map<String, Any>): Result<Unit> {
        val result = saleRepo.updateSale(saleId, updatedSale)
        if (result.isSuccess) {
            refreshSales()
        }
        return result
    }

    suspend fun deleteSale(saleId: String): Result<Unit> {
        val result = saleRepo.deleteSale(saleId)
        if (result.isSuccess) {
            refreshSales()
        }
        return result
    }
}