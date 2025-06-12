package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.AddCategoryRequest
import com.example.documentmanagerapp.utils.data.ApiResponse
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.data.Document
import com.example.documentmanagerapp.utils.data.SingleDocumentResponse
import retrofit2.Response
import retrofit2.http.*

interface CategoryApiService {
    @GET("categories/user/{userId}")
    suspend fun getCategories(@Path("userId") userId: Long): ApiResponse<List<Category>>

    @GET("documents/user/{userId}")
    suspend fun getDocuments(@Path("userId") userId: Long): ApiResponse<List<Document>>

    @POST("categories")
    suspend fun addCategory(@Body request: AddCategoryRequest): ApiResponse<Category>

    @PUT("categories/{categoryId}")
    suspend fun updateCategory(
        @Path("categoryId") categoryId: Long,
        @Body request: AddCategoryRequest
    ): ApiResponse<Category>

    @HTTP(method = "DELETE", path = "categories/{categoryId}", hasBody = false)
    suspend fun deleteCategory(@Path("categoryId") categoryId: Long): Response<ApiResponse<Unit?>>

    @GET("categories/{categoryId}")
    suspend fun getCategoryById(@Path("categoryId") categoryId: Long): ApiResponse<Category>

    @PATCH("documents/{documentId}/toggle-favorite")
    suspend fun toggleFavorite(@Path("documentId") documentId: Long): ApiResponse<SingleDocumentResponse>
}