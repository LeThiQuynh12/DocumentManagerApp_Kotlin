package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.BookmarkData
import com.example.documentmanagerapp.utils.data.BookmarkResponse
import com.example.documentmanagerapp.utils.service.BookmarkApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class BookmarkRepository(context: Context) {
    private val apiService: BookmarkApiService =
        ApiClient.getClient(context).create(BookmarkApiService::class.java)

//    suspend fun getBookmarksByUserId(userId: Long): List<BookmarkData> {
//        return withContext(Dispatchers.IO) {
//            try {
//                val response = apiService.getBookmarksByUserId(userId)
//                response.results ?: emptyList()  // ✅ OK vì .results thuộc BookmarkResponse
//            } catch (e: Exception) {
//                Log.e("BookmarkRepository", "Error fetching bookmarks: ${e.message}")
//                throw Exception("Lỗi lấy danh sách bookmark: ${e.message}")
//            }
//        }
//    }
suspend fun getBookmarksByUserId(userId: Long): List<BookmarkData> {
    return withContext(Dispatchers.IO) {
        try {
            val response = apiService.getBookmarksByUserId(userId)
            response.results ?: emptyList()
        } catch (e: Exception) {
            Log.e("BookmarkRepository", "Error fetching bookmarks: ${e.message}")
            throw Exception("Lỗi lấy danh sách bookmark: ${e.message}")
        }
    }
}


    suspend fun createBookmark(bookmark: BookmarkData): BookmarkData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.createBookmark(bookmark)
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error creating bookmark: ${e.message}")
                throw Exception("Lỗi tạo bookmark: ${e.message}")
            }
        }
    }

    suspend fun deleteBookmark(bookmarkId: Long) {
        withContext(Dispatchers.IO) {
            try {
                apiService.deleteBookmark(bookmarkId)
                Log.d("BookmarkRepository", "Deleted bookmark ID: $bookmarkId")
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error deleting bookmark: ${e.message}")
                throw Exception("Lỗi xóa bookmark: ${e.message}")
            }
        }
    }


    suspend fun toggleFavorite(docId: Long, userId: Long): BookmarkData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.toggleFavorite(docId, userId)
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error toggling favorite: ${e.message}")
                throw Exception("Lỗi chuyển đổi yêu thích: ${e.message}")
            }
        }
    }

    suspend fun updateBookmark(bookmarkId: Long, bookmark: BookmarkData): BookmarkData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.updateBookmark(bookmarkId, bookmark)
            } catch (e: Exception) {
                Log.e("BookmarkRepository", "Error updating bookmark: ${e.message}")
                throw Exception("Lỗi cập nhật bookmark: ${e.message}")
            }
        }
    }

}
