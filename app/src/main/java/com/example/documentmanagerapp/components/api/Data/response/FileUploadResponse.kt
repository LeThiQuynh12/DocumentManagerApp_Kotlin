package com.example.documentmanagerapp.components.api.Data.response


data class FileUploadResponse(
    val s3Url: String,
    val fileSize: Long,
    val documentId: Long?,
    val versionId: Long?
)
