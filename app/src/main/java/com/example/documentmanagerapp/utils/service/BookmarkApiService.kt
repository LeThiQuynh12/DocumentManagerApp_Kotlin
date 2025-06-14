package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.data.BookmarkResponse
import retrofit2.http.*

interface BookmarkApiService {
    @GET("bookmarks/user/{userId}")
    suspend fun getBookmarksByUserId(
        @Path("userId") userId: Long,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("sortDir") sortDir: String = "desc"
    ): BookmarkResponse

    @DELETE("bookmarks/{id}")
    suspend fun deleteBookmark(@Path("id") id: Long)
}