package com.k5.omsetku.model

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.parcelize.Parcelize

@Parcelize
data class Category(
    @DocumentId
    var categoryId: String = "",
    val categoryName: String = ""
): Parcelable {
    override fun toString(): String {
        return categoryName
    }
}