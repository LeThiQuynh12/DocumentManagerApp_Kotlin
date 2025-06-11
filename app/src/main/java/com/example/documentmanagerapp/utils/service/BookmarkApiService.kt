package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.data.BookmarkResponse
import com.example.documentmanagerapp.utils.data.Category
import retrofit2.http.*

interface BookmarkApiService {

    // Tạo một bookmark mới
    @POST("bookmarks")
    suspend fun createBookmark(@Body bookmark: BookmarkData): BookmarkData

    // Lấy tất cả bookmark của người dùng
    @GET("bookmarks/user/{userId}")
    suspend fun getBookmarksByUserId(
        @Path("userId") userId: Long,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): BookmarkResponse  // ✅ Phải là BookmarkResponse



    // Lấy bookmark theo docId và userId
    @GET("bookmarks/document/{docId}")
    suspend fun getBookmarksByDocIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long
    ): BookmarkResponse

    // Xoá bookmark theo id
    @DELETE("bookmarks/{id}")
    suspend fun deleteBookmark(@Path("id") id: Long)

    // Cập nhật bookmark theo id
    @PUT("bookmarks/{id}")
    suspend fun updateBookmark(@Path("id") id: Long, @Body updatedBookmark: BookmarkData): BookmarkData

    // Lấy bookmark theo id
    @GET("bookmarks/{id}")
    suspend fun getBookmarkById(@Path("id") id: Long): BookmarkData

    // Toggle favorite theo docId và userId
    @PATCH("bookmarks/documents/{docId}/toggle-favorite")
    suspend fun toggleFavorite(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long
    ): BookmarkData


    // Xoá bookmark theo docId và userId
    @DELETE("bookmarks/document/{docId}")
    suspend fun deleteBookmarkByDocumentIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long,
        @Query("password") password: String? = null
    )

    // Cập nhật bookmark theo docId và userId
    @PUT("bookmarks/document/{docId}")
    suspend fun updateBookmarkByDocumentIdAndUserId(
        @Path("docId") docId: Long,
        @Query("userId") userId: Long,
        @Body updatedBookmark: BookmarkData
    ): BookmarkResponse
}

