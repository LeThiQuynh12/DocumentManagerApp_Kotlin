package com.example.documentmanagerapp.components.api

import com.example.documentmanagerapp.components.api.Service.DocumentApiService
object RetrofitInstance {
    val api: DocumentApiService by lazy {
        ApiClient.create { SecureStorage.getToken() }  // Lấy token trực tiếp từ SecureStorage
            .create(DocumentApiService::class.java)
    }
}
