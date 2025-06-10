package com.example.documentmanagerapp.components.api.Auth

import com.example.documentmanagerapp.components.api.Data.request.LoginRequestDTO
import com.example.documentmanagerapp.components.api.Data.response.LoginResponseDTO
import com.example.documentmanagerapp.components.api.Data.response.UserResponseDTO
import retrofit2.Call
import retrofit2.http.*

interface AuthApiService {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequestDTO): Call<LoginResponseDTO>

    @GET("auth/refresh")
    fun refreshToken(@Header("Cookie") refreshTokenCookie: String): Call<LoginResponseDTO>

    @GET("auth/logout")
    fun logout(): Call<Void>

    @GET("auth/account")
    fun getAccount(): Call<UserResponseDTO>
}





