package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.DocumentData
import com.example.documentmanagerapp.utils.data.DocumentLinkRequest
import com.example.documentmanagerapp.utils.data.DocumentResponse
import retrofit2.http.*

interface DocumentApiService {

    @GET("documents")
    suspend fun getAllDocuments(): DocumentResponse

    @GET("documents/{id}")
    suspend fun getDocumentById(@Path("id") id: Long): DocumentResponse

    @GET("documents/shared/{userId}")
    suspend fun getSharedDocuments(@Path("userId") userId: Long): DocumentResponse

    @POST("documents")
    suspend fun createDocument(@Body document: DocumentLinkRequest): DocumentData

    @PUT("documents/{id}")
    suspend fun updateDocument(@Path("id") id: Long, @Body document: DocumentData): DocumentData

    @DELETE("documents/{id}")
    suspend fun deleteDocument(@Path("id") id: Long)

    @GET("documents/user/{userId}")
    suspend fun getDocumentsByUserId(@Path("userId") userId: Long): DocumentResponse

    @GET("documents/category/{categoryId}")
    suspend fun getDocumentsByCategoryId(@Path("categoryId") categoryId: Long): DocumentResponse

    @GET("documents/search/by-name")
    suspend fun searchDocumentsByName(@Query("name") name: String): DocumentResponse

    @GET("documents/search/by-filetype")
    suspend fun searchDocumentsByFileType(@Query("fileType") fileTypes: List<String>): DocumentResponse

    @GET("documents/search")
    suspend fun searchDocumentsByKeyword(@Query("keyword") keyword: String): DocumentResponse

    @PATCH("documents/{id}/toggle-favorite")
    suspend fun toggleFavorite(@Path("id") id: Long): Map<String, Any>

    @GET("documents/user/{userId}/favorites")
    suspend fun getFavoriteDocuments(@Path("userId") userId: Long): DocumentResponse
}
