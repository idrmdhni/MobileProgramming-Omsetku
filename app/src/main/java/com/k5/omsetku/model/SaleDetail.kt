package com.k5.omsetku.model

import com.google.firebase.firestore.DocumentId

data class SaleDetail(
    @DocumentId
    var saleDetailId: String = "",
    val productId: String = "",
    val productPrice: Long = 0,
    val quantity: Int = 0,
    val subTotal: Long = 0
)
