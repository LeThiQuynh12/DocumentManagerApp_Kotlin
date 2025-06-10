package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Auth.AuthApiService
import com.example.documentmanagerapp.components.api.Data.request.LoginRequestDTO
import com.example.documentmanagerapp.components.api.Data.response.LoginResponseDTO
import com.example.documentmanagerapp.components.api.SecureStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

fun loginAndSaveToken(
    authApi: AuthApiService,
    loginRequest: LoginRequestDTO,
    onSuccess: () -> Unit,
    onError: (Throwable) -> Unit
) {
    authApi.login(loginRequest).enqueue(object : Callback<LoginResponseDTO> {
        override fun onResponse(
            call: Call<LoginResponseDTO>,
            response: Response<LoginResponseDTO>
        ) {
            if (response.isSuccessful) {
                val loginResponse = response.body()
                val token = loginResponse?.accessToken
                if (!token.isNullOrEmpty()) {
                    // Lưu token vào SecureStorage
                    SecureStorage.saveToken(token)
                    onSuccess()
                } else {
                    onError(Throwable("Token is empty"))
                }
            } else {
                onError(Throwable("Login failed with code: ${response.code()}"))
            }
        }

        override fun onFailure(call: Call<LoginResponseDTO>, t: Throwable) {
            onError(t)
        }
    })
}
