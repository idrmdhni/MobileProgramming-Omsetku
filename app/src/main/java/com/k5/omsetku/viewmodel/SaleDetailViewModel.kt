package com.k5.omsetku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k5.omsetku.model.Product
import com.k5.omsetku.model.Sale
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.repository.ProductRepository
import com.k5.omsetku.repository.SaleDetailRepository
import com.k5.omsetku.repository.SaleRepository
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class SaleDetailViewModel: ViewModel() {
    private val saleDetailRepo = SaleDetailRepository()

    // MutableLiveData untuk mengelola state data (loading, success, error)
    private val _salesDetails =  MutableLiveData<LoadState<List<SaleDetail>>>()
    val salesDetails: LiveData<LoadState<List<SaleDetail>>> = _salesDetails

    // Variabel untuk menandakan apakah data sudah dimuat setidaknya sekali
    private var isDataLoaded = false

    init {
        loadSalesDetails()
    }

    fun loadSalesDetails(saleId: String = "", forceRefresh: Boolean = false) {
        // Jika data sudah dimuat dan tidak ada paksaan refresh, jangan muat ulang
        if (isDataLoaded && !forceRefresh && _salesDetails.value is LoadState.Success) {
            return
        }

        _salesDetails.value = LoadState.Loading

        viewModelScope.launch {
            val result = saleDetailRepo.getSalesDetails(saleId)
            result.onSuccess { saleList ->
                _salesDetails.value = LoadState.Success(saleList)
                isDataLoaded = true // Tandai bahwa data sudah dimuat
            }.onFailure { e ->
                _salesDetails.value = LoadState.Error(e.message ?: "Unknown error")
                isDataLoaded = false // Reset isDataLoaded jika ada error agar bisa coba lagi
            }
        }
    }

    // Fungsi untuk memuat ulang data secara paksa (misalnya, setelah edit/hapus)
    fun refreshSalesDetails() {
        loadSalesDetails(forceRefresh =  true)
    }

    suspend fun addSaleDetail (saleId: String, saleDetail: SaleDetail): Result<SaleDetail> {
        val result = saleDetailRepo.addSaleDetail(saleId, saleDetail)
        if (result.isSuccess) {
            refreshSalesDetails()
        }
        return result
    }

    suspend fun deleteSale(saleId: String, saleDetailId: String): Result<Unit> {
        val result = saleDetailRepo.deleteSaleDetail(saleId, saleDetailId)
        if (result.isSuccess) {
            refreshSalesDetails()
        }
        return result
    }
}