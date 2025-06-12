package com.example.documentmanagerapp.utils.data

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class BookmarkData(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("user")
    val user: UserData,  // Bạn cần tạo class UserData nếu chưa có

    @SerializedName("document")
    val document: DocumentData,  // Bạn cần tạo class DocumentData nếu chưa có

    @SerializedName("category")
    val category: Category? = null,

    @SerializedName("isFavorite")
    val isFavorite: Boolean = false,

    @SerializedName("createdAt")
    val createdAt: Timestamp? = null
)
data class BookmarkResponse(
    val results: List<BookmarkData>?
)