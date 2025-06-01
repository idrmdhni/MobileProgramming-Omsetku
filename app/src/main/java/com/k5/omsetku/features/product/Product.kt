package com.k5.omsetku.features.product

data class Product(
    val productId: String,
    val productName: String,
    val productStock: Int,
    val productPrice: Int,
    val categoryId: String
)
