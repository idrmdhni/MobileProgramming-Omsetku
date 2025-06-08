package com.k5.omsetku.utils

sealed class LoadState<out T> {
    object Loading : LoadState<Nothing>()
    data class Success<out T>(val data: T) : LoadState<T>()
    data class Error(val message: String) : LoadState<Nothing>()
}