package com.example.documentmanagerapp.components.api.Data
import com.example.documentmanagerapp.components.api.ultil.RoleEnum
import java.time.Instant
data class User(
    val id: Long? = null,
    val fullName: String,
    val email: String,
    val password: String,
    val role: RoleEnum,
    val status: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Instant? = null,
    val refreshToken: String? = null
)
