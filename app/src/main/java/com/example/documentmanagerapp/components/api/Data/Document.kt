package com.example.documentmanagerapp.components.api.Data
import java.sql.Timestamp

data class Document(
    val id: Long? = null,
    val user: User,
    val documentName: String,
    val fileType: String,
    val fileUrl: String? = null,
    val password: String? = null,
    val encryptionMethod: String? = null,
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null,
    val category: Category? = null,
    val isFavorite: Boolean = false
)