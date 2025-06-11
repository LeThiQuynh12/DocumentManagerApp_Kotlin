package com.example.documentmanagerapp.utils.data

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Long,
    @SerializedName("categoryName") val name: String,
    @SerializedName("categoryGroup") val group: String // MAIN_BOOSTER hoặc ANOTHER_SAVED_LIST
)

data class CategoryResponse(
    val results: List<Category>? // Cho API trả về danh sách
)

data class SingleCategoryResponse(
    val results: Category? // Cho API trả về một đối tượng
)

data class Document(
    val id: Long,
    val category: Category?
)

data class DocumentResponse(
    val results: List<Document>?
)

data class AddCategoryRequest(
    @SerializedName("categoryName") val name: String,
    @SerializedName("categoryGroup") val group: String,
    @SerializedName("user") val user: User
) {
    data class User(
        @SerializedName("id") val id: Long
    )
}