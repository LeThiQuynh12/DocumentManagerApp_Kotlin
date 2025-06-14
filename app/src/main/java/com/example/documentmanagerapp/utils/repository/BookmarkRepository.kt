package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.data.BookmarkResponse
import com.example.documentmanagerapp.utils.service.BookmarkApiService
import com.example.documentmanagerapp.utils.data.Document
import com.example.documentmanagerapp.utils.service.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class BookmarkRepository(context: Context) {
    private val apiService: BookmarkApiService =
        ApiClient.getClient(context).create(BookmarkApiService::class.java)
    private val categoryApiService: CategoryApiService =
        ApiClient.getClient(context).create(CategoryApiService::class.java)

    suspend fun getBookmarksByUserId(userId: Long): List<BookmarkData> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("BookmarkRepository", "Fetching bookmarks for userId: $userId")
                val response = apiService.getBookmarksByUserId(userId)
                response.results ?: emptyList()
            } catch (e: HttpException) {
                Log.e("BookmarkRepository", "HTTP error: ${e.code()}, ${e.message()}")
                when (e.code()) {
                    404 -> emptyList()
                    else -> throw Exception("Lỗi lấy danh sách bookmark: ${e.message()}")
                }
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error fetching bookmarks: ${e.message}")
                throw Exception("Lỗi lấy danh sách bookmark: ${e.message}")
            }
        }
    }

    suspend fun toggleFavorite(documentId: Long): Document {
        return withContext(Dispatchers.IO) {
            try {
                val response = categoryApiService.toggleFavorite(documentId)
                val updatedDocument = response.results?.results
                    ?: throw Exception("Invalid toggle favorite response")
                Log.d("BookmarkRepository", "Toggled favorite for document: $updatedDocument")
                updatedDocument
            } catch (e: HttpException) {
                Log.e("BookmarkRepository", "HTTP error toggling favorite: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error toggling favorite: ${e.message}")
                throw Exception("Lỗi cập nhật trạng thái yêu thích: ${e.message}")
            }
        }
    }
}