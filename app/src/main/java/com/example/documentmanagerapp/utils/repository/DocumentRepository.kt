package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.DocumentData
import com.example.documentmanagerapp.utils.data.DocumentLinkRequest
import com.example.documentmanagerapp.utils.service.DocumentApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class DocumentRepository(context: Context) {
    private val apiService: DocumentApiService =
        ApiClient.getClient(context).create(DocumentApiService::class.java)

    suspend fun getDocumentById(documentId: Long): DocumentData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentById(documentId)
                Log.d("DocumentRepository", "Fetched document by ID $documentId: $response")
                response.results
            } catch (e: HttpException) {
                Log.e("DocumentRepository", "HTTP error fetching document by ID $documentId: ${e.code()} - ${e.message()}")
                null
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error fetching document by ID $documentId: ${e.message}")
                null
            }
        }
    }

    suspend fun getDocumentsByUser(userId: Long): List<DocumentData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocumentsByUserId(userId)
                val documents = response.results ?: emptyList()
                Log.d("DocumentRepository", "Fetched documents: $documents")
                documents
            } catch (e: HttpException) {
                Log.e("DocumentRepository", "HTTP error: ${e.code()} - ${e.message()}")
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error: ${e.message}")
                throw Exception("Lỗi kết nối: ${e.message}")
            }
        }
    }

    suspend fun addDocument(document: DocumentLinkRequest): DocumentData? {
        return withContext(Dispatchers.IO) {
            try {
                val newDocument = apiService.createDocument(document)
                Log.d("DocumentRepository", "Added document: $newDocument")
                newDocument
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error adding document: ${e.message}")
                throw Exception("Lỗi thêm tài liệu: ${e.message}")
            }
        }
    }

    suspend fun updateDocument(documentId: Long, request: DocumentData): DocumentData? {
        return withContext(Dispatchers.IO) {
            try {
                val updatedDocument = apiService.updateDocument(documentId, request)
                Log.d("DocumentRepository", "Updated document: $updatedDocument")
                updatedDocument
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error updating document: ${e.message}")
                throw Exception("Lỗi cập nhật tài liệu: ${e.message}")
            }
        }
    }

    suspend fun deleteDocument(documentId: Long) {
        withContext(Dispatchers.IO) {
            try {
                apiService.deleteDocument(documentId)
                Log.d("DocumentRepository", "Deleted document: $documentId")
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error deleting document: ${e.message}")
                throw Exception("Lỗi xóa tài liệu: ${e.message}")
            }
        }
    }

    suspend fun searchDocumentsByKeyword(keyword: String): List<DocumentData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.searchDocumentsByKeyword(keyword)
                val documents = response.results ?: emptyList()
                Log.d("DocumentRepository", "Searched documents by keyword: $documents")
                documents
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error searching documents: ${e.message}")
                throw Exception("Lỗi tìm kiếm tài liệu: ${e.message}")
            }
        }
    }

    suspend fun searchDocumentsByFileType(fileType: String): List<DocumentData> {
        return withContext(Dispatchers.IO) {
            try {
                // Ánh xạ fileType từ frontend sang backend
                val fileTypeMapping = mapOf(
                    "url" to "Link",
                    "png" to "Image",
                    "mp4" to "Video",
                    "document" to "Document"
                )
                val mappedFileType = fileTypeMapping[fileType] ?: fileType
                val documents = apiService.searchDocumentsByFileType(mappedFileType)
                Log.d("DocumentRepository", "Searched documents by file type: $documents")
                documents
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error searching by file type: ${e.message}")
                throw Exception("Lỗi tìm kiếm theo loại file: ${e.message}")
            }
        }
    }

    suspend fun toggleFavorite(documentId: Long): Map<String, Any> {
        return withContext(Dispatchers.IO) {
            try {
                val result = apiService.toggleFavorite(documentId)
                Log.d("DocumentRepository", "Toggled favorite for document $documentId: $result")
                result
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error toggling favorite: ${e.message}")
                throw Exception("Lỗi đánh dấu yêu thích: ${e.message}")
            }
        }
    }

    suspend fun getFavoriteDocuments(userId: Long): List<DocumentData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getFavoriteDocuments(userId)
                val favorites = response.results ?: emptyList()
                Log.d("DocumentRepository", "Fetched favorites: $favorites")
                favorites
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error getting favorites: ${e.message}")
                throw Exception("Lỗi tải tài liệu yêu thích: ${e.message}")
            }
        }
    }

    // Optional: fetch documents + favorites together
    suspend fun fetchUserDocumentsWithFavorites(userId: Long): Pair<List<DocumentData>, List<DocumentData>> {
        return withContext(Dispatchers.IO) {
            try {
                val documentsDeferred = async { apiService.getDocumentsByUserId(userId) }
                val favoritesDeferred = async { apiService.getFavoriteDocuments(userId) }

                val documents = documentsDeferred.await().results ?: emptyList()
                val favorites = favoritesDeferred.await().results ?: emptyList()

                Log.d("DocumentRepository", "Documents: $documents, Favorites: $favorites")
                documents to favorites
            } catch (e: Exception) {
                Log.e("DocumentRepository", "Error fetching documents and favorites: ${e.message}")
                throw Exception("Lỗi tải dữ liệu tài liệu: ${e.message}")
            }
        }
    }
}