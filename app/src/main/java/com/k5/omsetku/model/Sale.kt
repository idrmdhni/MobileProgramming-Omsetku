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
    var totalPurchase: Long = 0,
    @ServerTimestamp
    val purchaseDate: Timestamp? = null,
    val month: Int? = null,
    val year: Int? = null,
): Parcelable