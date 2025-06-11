package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.CategoryResponse
import com.example.documentmanagerapp.utils.data.DocumentResponse
import com.example.documentmanagerapp.utils.data.AddCategoryRequest
import retrofit2.http.*

interface CategoryApiService {
    @GET("categories/user/{userId}")
    suspend fun getCategories(@Path("userId") userId: Long): CategoryResponse

    @GET("documents/user/{userId}")
    suspend fun getDocuments(@Path("userId") userId: Long): DocumentResponse

    @POST("categories")
    suspend fun addCategory(@Body request: AddCategoryRequest): CategoryResponse

    @PUT("categories/{categoryId}")
    suspend fun updateCategory(@Path("categoryId") categoryId: Long, @Body request: AddCategoryRequest): CategoryResponse

    @DELETE("categories/{categoryId}")
    suspend fun deleteCategory(@Path("categoryId") categoryId: Long)
}