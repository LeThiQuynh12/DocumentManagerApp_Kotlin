package com.example.documentmanagerapp.utils.data

import com.google.gson.annotations.SerializedName

data class Category(
    val id: Long,
    @SerializedName("categoryName") val name: String,
    @SerializedName("categoryGroup") val group: String // MAIN_BOOSTER hoặc ANOTHER_SAVED_LIST
)

data class CategoryResponse(
    val results: List<Category>?
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
    @SerializedName("categoryGroup") val group: String
)