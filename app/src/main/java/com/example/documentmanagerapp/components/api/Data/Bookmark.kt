package com.example.documentmanagerapp.components.api.Data
import java.sql.Timestamp

data class Bookmark(
    val id: Long?,
    val user: User,
    val document: Document,
    val category: Category?,
    val isFavorite: Boolean = false,
    val createdAt: Timestamp?
)