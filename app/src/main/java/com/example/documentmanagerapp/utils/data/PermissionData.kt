package com.example.documentmanagerapp.utils.data


import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class PermissionData(

    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("user")
    val user: UserData,

    @SerializedName("document")
    val document: DocumentData, // Cần class DocumentData

    @SerializedName("permissionType")
    val permissionType: PermissionType,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

enum class PermissionType {
    READ,
    EDIT
}
data class ShareRequest(

    @SerializedName("email")
    val email: String,

    @SerializedName("documentId")
    val documentId: Long,

    @SerializedName("permissionType")
    val permissionType: PermissionType
)
data class ShareRequestData(

    @SerializedName("email")
    val email: String,

    @SerializedName("documentId")
    val documentId: String,

    @SerializedName("permissionType")
    val permissionType: PermissionType
)

data class UpdatePermissionRequestData(

    @SerializedName("email")
    val email: String,

    @SerializedName("documentId")
    val documentId: Long,

    @SerializedName("permissionType")
    val permissionType: PermissionType
)

data class PermissionsResponse(
    @SerializedName("error") val error: String?,
    @SerializedName("message") val message: String,
    @SerializedName("results") val permissions: List<PermissionData>,
    @SerializedName("status_code") val statusCode: Int
)
