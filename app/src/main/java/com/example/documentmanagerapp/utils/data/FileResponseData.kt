package com.example.documentmanagerapp.utils.data
import com.google.gson.annotations.SerializedName

data class FileUploadResponse(
    @SerializedName("s3Url") val s3Url: String,
    @SerializedName("fileSize") val fileSize: Long,
    @SerializedName("documentId") val documentId: Long?,
    @SerializedName("versionId") val versionId: Long?
)
