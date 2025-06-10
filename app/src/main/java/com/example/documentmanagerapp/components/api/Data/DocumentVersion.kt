package com.example.documentmanagerapp.components.api.Data
import java.sql.Timestamp

data class DocumentVersion(
    val id: Long? = null,
    val document: Document,
    val versionNumber: Int,
    val s3Url: String,
    val fileSize: Long,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
)