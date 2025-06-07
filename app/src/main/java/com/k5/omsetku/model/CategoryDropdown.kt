package com.k5.omsetku.model


data class CategoryDropdown(
    var categoryId: String = "",
    val categoryName: String = ""
) {
    override fun toString(): String {
        return categoryName
    }
}
