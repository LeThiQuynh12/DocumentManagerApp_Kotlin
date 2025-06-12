package com.example.documentmanagerapp.utils.data

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class DocumentData(
    val id: Long,

    @SerializedName("documentName")
    val documentName: String,

    @SerializedName("fileType")
    val fileType: String,

    @SerializedName("fileUrl")
    val fileUrl: String?,

    @SerializedName("password")
    val password: String?,

    @SerializedName("encryptionMethod")
    val encryptionMethod: String?,

    @SerializedName("createdAt")
    val createdAt: Timestamp,

    @SerializedName("updatedAt")
    val updatedAt: Timestamp,

    @SerializedName("isFavorite")
    val isFavorite: Boolean,

    @SerializedName("category")
    val category: Category?,

    @SerializedName("user")
    val user: UserData? // Khuyên nên tạo class UserData riêng nếu cần
)

data class DocumentResponse(
    val results: List<DocumentData>?
)
data class UserData(
    val id: Long,
    val username: String // Thêm các trường khác nếu cần
)
data class DocumentLinkRequest(
    @SerializedName("documentName")
    val documentName: String,

    @SerializedName("fileType")
    val fileType: String,

    @SerializedName("fileUrl")
    val fileUrl: String?,

    @SerializedName("password")
    val password: String?,

    @SerializedName("encryptionMethod")
    val encryptionMethod: String?,

    @SerializedName("user")
    val user: UserIdRequest,

    @SerializedName("category")
    val category: CategoryIdRequest
)

data class UserIdRequest(
    @SerializedName("id")
    val id: Long
)

data class CategoryIdRequest(
    @SerializedName("id")
    val id: Long
)