package com.k5.omsetku.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var productId: String = "",
    val productName: String = "",
    var productStock: Int = 0,
    val productPrice: Long = 0,
    val categoryId: String = ""
): Parcelable