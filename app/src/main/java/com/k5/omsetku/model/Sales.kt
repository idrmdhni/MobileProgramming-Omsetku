package com.k5.omsetku.model

data class Sales(
    var invoiceNumberId: String = "",
    val buyersName: String = "",
    val invoiceNumber: String = "",
    val purchaseDate: String = "",
    val totalPurchase: Long = 0,
)