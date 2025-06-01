package com.k5.omsetku.features.sales

data class Sales(
    val invoiceNumberId: String,
    val buyersName: String,
    val invoiceNumber: String,
    val purchaseDate: String,
    val totalPurchase: Long,
)
