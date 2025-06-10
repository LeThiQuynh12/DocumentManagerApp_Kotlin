package com.example.documentmanagerapp.components.api.Data.response

data class UserResponseDTO(
    val id: Long? = null,
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val gender: String? = null,
    val role: String? = null,     // STUDENT | LECTURER | ADMIN
    val status: String? = null,
    val avatarUrl: String? = null
)
