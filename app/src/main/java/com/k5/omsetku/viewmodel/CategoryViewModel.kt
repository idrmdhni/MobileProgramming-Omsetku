package com.k5.omsetku.viewmodel

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.k5.omsetku.R
import com.k5.omsetku.fragment.CategoryFragment
import com.k5.omsetku.model.Category
import com.k5.omsetku.repository.CategoryRepository
import com.k5.omsetku.utils.LoadFragment
import com.k5.omsetku.utils.LoadState
import kotlinx.coroutines.launch

class CategoryViewModel: ViewModel() {
    private val categoryRepo = CategoryRepository()

    // MutableLiveData untuk mengelola state data (loading, success, error)
    private val _categories =  MutableLiveData<LoadState<List<Category>>>()
    val categories: LiveData<LoadState<List<Category>>> = _categories

    // Variabel untuk menandakan apakah data sudah dimuat setidaknya sekali
    private var isDataLoaded = false

    init {
        loadCategories()
    }

    fun loadCategories(forceRefresh: Boolean = false) {
        // Jika data sudah dimuat dan tidak ada paksaan refresh, jangan muat ulang
        if (isDataLoaded && !forceRefresh && _categories.value is LoadState.Success) {
            return
        }

        _categories.value = LoadState.Loading

        viewModelScope.launch {
            val result = categoryRepo.getCategories()
            result.onSuccess { categoryList ->
                _categories.value = LoadState.Success(categoryList)
                isDataLoaded = true // Tandai bahwa data sudah dimuat
            }.onFailure { e ->
                _categories.value = LoadState.Error(e.message ?: "Unknown error")
                isDataLoaded = false // Reset isDataLoaded jika ada error agar bisa coba lagi
            }
        }
    }

    // Fungsi untuk memuat ulang data secara paksa (misalnya, setelah edit/hapus)
    fun refreshCategories() {
        loadCategories(true)
    }

    suspend fun addCategory (categoryName: String): Result<Category> {
        val result = categoryRepo.addCategory(categoryName)
        if (result.isSuccess) {
            refreshCategories()
        }
        return result
    }

    suspend fun updateCategory (categoryId: String, newCategoryName: String): Result<Unit> {
        val result = categoryRepo.updateCategory(categoryId, newCategoryName)
        if (result.isSuccess) {
            refreshCategories()
        }
        return result
    }

    suspend fun deleteCategory(categoryId: String): Result<Unit> {
        val result = categoryRepo.deleteCategory(categoryId)
        if (result.isSuccess) {
            refreshCategories()
        }
        return result
    }
}