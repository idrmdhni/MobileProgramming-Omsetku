package com.k5.omsetku.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.Timestamp as Timestamp

data class Sale(
    @DocumentId
    var saleId: String = "",
    val buyersName: String = "",
    val invoiceNumber: String = "",
    val purchaseDate: Timestamp = Timestamp.now(),
    val totalPurchase: Long = 0,
)