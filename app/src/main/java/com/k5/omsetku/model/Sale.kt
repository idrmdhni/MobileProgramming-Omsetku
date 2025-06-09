package com.k5.omsetku.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.parcelize.Parcelize
import com.google.firebase.Timestamp as Timestamp

@Parcelize
data class Sale(
    @DocumentId
    var saleId: String = "",
    val buyersName: String = "",
    val invoiceNumber: String = "",
    @ServerTimestamp
    val purchaseDate: Timestamp? = null,
    val totalPurchase: Long = 0,
): Parcelable