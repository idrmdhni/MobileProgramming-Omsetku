package com.k5.omsetku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.k5.omsetku.model.Sale
import com.k5.omsetku.model.SaleDetail
import com.k5.omsetku.repository.SaleRepository
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class SaleViewModel: ViewModel() {
    private val saleRepo = SaleRepository()

    // MutableLiveData untuk mengelola state data (loading, success, error)
    private val _sales =  MutableLiveData<LoadState<List<Sale>>>()
    val sales: LiveData<LoadState<List<Sale>>> = _sales

    // LiveData baru untuk daftar tahun unik
    private val _availableYears = MutableLiveData<LoadState<List<Int>>>()
    val availableYears: LiveData<LoadState<List<Int>>> = _availableYears

    // LiveData baru untuk daftar bulan unik (format angka, 1-12)
    private val _availableMonths = MutableLiveData<LoadState<List<Int>>>()
    val availableMonths: LiveData<LoadState<List<Int>>> = _availableMonths

    // Variabel untuk menandakan apakah data sudah dimuat setidaknya sekali
    private var isDataLoaded = false

    init {
        loadSales()
        loadAvailableFilterOptions()
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

    // Fungsi baru untuk memuat daftar tahun dan bulan yang tersedia
    fun loadAvailableFilterOptions() {
        _availableYears.value = LoadState.Loading
        _availableMonths.value = LoadState.Loading

        viewModelScope.launch {
            val result = saleRepo.getSales() // Ambil semua penjualan untuk mendapatkan tahun/bulan unik
            result.onSuccess { saleList ->
                extractAndSetUniqueYearsMonths(saleList)
            }.onFailure { e ->
                _availableYears.value = LoadState.Error(e.message ?: "Unknown error")
                _availableMonths.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Helper function untuk mengekstrak tahun dan bulan unik
    private fun extractAndSetUniqueYearsMonths(sales: List<Sale>) {
        val uniqueYears = sales
            .mapNotNull { it.year } // Ambil semua tahun (non-null)
            .distinct()              // Ambil yang unik
            .sortedDescending()      // Urutkan dari terbaru ke terlama
        _availableYears.value = LoadState.Success(uniqueYears)

        val uniqueMonths = sales
            .mapNotNull { it.month } // Ambil semua bulan (non-null)
            .distinct()              // Ambil yang unik
            .sorted()                // Urutkan dari 1 sampai 12
        _availableMonths.value = LoadState.Success(uniqueMonths)
    }

    fun loadSalesByMonthAndYear(month: Int? = null, year: Int? = null) {
        _sales.value = LoadState.Loading
        viewModelScope.launch {
            val result = saleRepo.getSalesByMonthAndYear(month, year)
            result.onSuccess { saleList ->
                _sales.value = LoadState.Success(saleList)
            }.onFailure { e ->
                _sales.value = LoadState.Error(e.message ?: "Unknown error")
            }
        }
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