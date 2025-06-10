package com.example.documentmanagerapp.components.api.Data.response
data class LoginResponseDTO(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val user: UserLogin? = null
) {
    data class UserLogin(
        val id: Long? = null,
        val email: String? = null,
        val fullName: String? = null
    )
}