package com.example.documentmanagerapp.components.api.Data

import com.example.documentmanagerapp.components.api.ultil.CategoryGroup
import java.sql.Timestamp

data class Category(
    val id: Long? = null,
    val user: User, // bạn cũng phải có data class User tương ứng
    val categoryName: String,
    val categoryGroup: CategoryGroup, // enum class Kotlin tương ứng
    val createdAt: String? = null, // dùng String cho dễ parse JSON, hoặc Instant
    val updatedAt: String? = null
)