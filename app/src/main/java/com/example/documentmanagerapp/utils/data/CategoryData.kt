package com.example.documentmanagerapp.utils.data

import com.example.documentmanagerapp.utils.User
import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String,
    @SerializedName("results") val results: T?,
    @SerializedName("status_code") val status_code: Int
)

data class Category(
    @SerializedName("id") val id: Long,
    @SerializedName("categoryName") val name: String,
    @SerializedName("categoryGroup") val group: String, // MAIN_BOOSTER hoặc ANOTHER_SAVED_LIST
    @SerializedName("createdAt") val createdAt: String? = null,
    @SerializedName("updatedAt") val updatedAt: String? = null
)

data class SingleCategoryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: User?,
    @SerializedName("categoryName") val name: String,
    @SerializedName("categoryGroup") val group: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String
) {
    fun toCategory(): Category = Category(
        id = id,
        name = name,
        group = group,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

data class Document(
    @SerializedName("id") val id: Long,
    @SerializedName("user") val user: User?,
    @SerializedName("documentName") val documentName: String,
    @SerializedName("fileType") val fileType: String,
    @SerializedName("fileUrl") val fileUrl: String?,
    @SerializedName("password") val password: String?,
    @SerializedName("encryptionMethod") val encryptionMethod: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("category") val category: Category?,
    @SerializedName("isFavorite") val isFavorite: Boolean
) {
    data class User(
        @SerializedName("id") val id: Long,
        @SerializedName("fullName") val fullName: String,
        @SerializedName("email") val email: String,
        @SerializedName("password") val password: String,
        @SerializedName("role") val role: String,
        @SerializedName("status") val status: String?,
        @SerializedName("avatarUrl") val avatarUrl: String?,
        @SerializedName("createdAt") val createdAt: String,
        @SerializedName("refreshToken") val refreshToken: String?
    )
}

data class SingleDocumentResponse(
    @SerializedName("results") val results: Document?
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