package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Data.Document
import retrofit2.Call
import retrofit2.http.*

interface DocumentApiService {

    @GET("documents")
    fun getAllDocuments(): Call<List<Document>>

    @GET("documents/{id}")
    fun getDocumentById(@Path("id") id: Long): Call<Document>

    @GET("documents/shared/{userId}")
    fun getSharedDocuments(@Path("userId") userId: Long): Call<List<Document>>

    @POST("documents")
    fun createDocument(@Body document: Document): Call<Document>

    @PUT("documents/{id}")
    fun updateDocument(@Path("id") id: Long, @Body document: Document): Call<Document>

    @GET("documents/user/{userId}")
    fun getDocumentsByUserId(@Path("userId") userId: Long): Call<List<Document>>

    @GET("documents/category/{categoryId}")
    fun getDocumentsByCategoryId(@Path("categoryId") categoryId: Long): Call<List<Document>>

    @GET("documents/search/by-name")
    fun searchDocumentsByName(@Query("name") name: String): Call<List<Document>>

    @GET("documents/search/by-filetype")
    fun searchDocumentsByFileType(@Query("fileType") fileTypes: List<String>): Call<List<Document>>

    @GET("documents/search")
    fun searchDocumentsByKeyword(@Query("keyword") keyword: String): Call<List<Document>>

    @PATCH("documents/{id}/toggle-favorite")
    fun toggleFavorite(@Path("id") id: Long): Call<Map<String, Any>>

    @GET("documents/user/{userId}/favorites")
    fun getFavoriteDocuments(@Path("userId") userId: Long): Call<List<Document>>
}
