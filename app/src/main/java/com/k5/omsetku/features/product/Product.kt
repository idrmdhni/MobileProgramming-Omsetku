package com.k5.omsetku.features.product

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    val productId: String,
    val productName: String,
    var productStock: Int,
    val productPrice: Int,
    val categoryId: String
): Parcelable
