package com.example.documentmanagerapp.utils.data

import com.example.documentmanagerapp.utils.User
import com.google.gson.annotations.SerializedName

data class BookmarkData(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("user") val user: User,
    @SerializedName("document") val document: Document,
    @SerializedName("category") val category: Category? = null,
    @SerializedName("isFavorite") val isFavorite: Boolean = false,
    @SerializedName("createdAt") val createdAt: String? = null
)

data class BookmarkResponse(
    @SerializedName("error") val error: String? = null,
    @SerializedName("message") val message: String? = null,
    @SerializedName("results") val results: List<BookmarkData>? = null,
    @SerializedName("status_code") val statusCode: Int? = null
)