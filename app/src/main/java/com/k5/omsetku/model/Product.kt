package com.k5.omsetku.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    @DocumentId
    var productId: String = "",
    val productName: String = "",
    var productStock: Int = 0,
    val productPrice: Long = 0,
    val productDescription: String = "",
    val categoryId: String = "",
    val lowStock: Int = 5
): Parcelable