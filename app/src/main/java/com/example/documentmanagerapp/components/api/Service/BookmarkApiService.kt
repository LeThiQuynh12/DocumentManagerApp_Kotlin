package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Data.Bookmark
import retrofit2.Call
import retrofit2.http.*

interface BookmarkApiService {

    // Tạo một bookmark mới
    @POST("bookmarks")
    fun createBookmark(@Body bookmark: Bookmark): Call<Bookmark>

    // Lấy tất cả bookmark của người dùng
    @GET("bookmarks/user/{userId}")
    fun getBookmarksByUserId(
        @Path("userId") userId: Long,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): Call<List<Bookmark>>

    // Lấy bookmark theo docId và userId
    @GET("bookmarks/document/{docId}")
    fun getBookmarksByDocIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long
    ): Call<List<Bookmark>>

    // Xoá bookmark theo id
    @DELETE("bookmarks/{id}")
    fun deleteBookmark(@Path("id") id: Long): Call<Void>

    // Cập nhật bookmark theo id
    @PUT("bookmarks/{id}")
    fun updateBookmark(@Path("id") id: Long, @Body updatedBookmark: Bookmark): Call<Bookmark>

    // Lấy bookmark theo id
    @GET("bookmarks/{id}")
    fun getBookmarkById(@Path("id") id: Long): Call<Bookmark>

    // Toggle favorite theo docId và userId
    @PATCH("bookmarks/documents/{docId}/toggle-favorite")
    fun toggleFavorite(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long
    ): Call<Bookmark>

    // Xoá bookmark theo docId và userId
    @DELETE("bookmarks/document/{docId}")
    fun deleteBookmarkByDocumentIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long,
        @Query("password") password: String? = null
    ): Call<Void>

    // Cập nhật bookmark theo docId và userId
    @PUT("bookmarks/document/{docId}")
    fun updateBookmarkByDocumentIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long,
        @Body updatedBookmark: Bookmark
    ): Call<Bookmark>
}