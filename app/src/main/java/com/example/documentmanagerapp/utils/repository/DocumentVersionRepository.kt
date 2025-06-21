package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.DocumentVersionData
import com.example.documentmanagerapp.utils.service.DocumentVersionApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DocumentVersionRepository(context: Context) {

    private val apiService: DocumentVersionApiService =
        ApiClient.getClient(context).create(DocumentVersionApiService::class.java)

    suspend fun getVersionsByDocumentId(docId: Long): List<DocumentVersionData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getVersionsByDocumentId(docId)
                response.results ?: emptyList() // Trả về danh sách rỗng nếu results là null
            } catch (e: Exception) {
                Log.e("DocumentVersionRepo", "Lỗi khi lấy phiên bản tài liệu: ${e.message}")
                throw Exception("Không thể lấy phiên bản tài liệu: ${e.message}")
            }
        }
    }

    suspend fun getVersions(documentId: Long): List<DocumentVersionData> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getVersions(documentId)
            } catch (e: Exception) {
                Log.e("DocumentVersionRepo", "Lỗi khi lấy phiên bản: ${e.message}")
                throw Exception("Không thể lấy phiên bản: ${e.message}")
            }
        }
    }

    suspend fun createDocumentVersion(version: DocumentVersionData): DocumentVersionData {
        return withContext(Dispatchers.IO) {
            try {
                apiService.createDocumentVersion(version)
            } catch (e: Exception) {
                Log.e("DocumentVersionRepo", "Lỗi khi tạo phiên bản mới: ${e.message}")
                throw Exception("Không thể tạo phiên bản mới: ${e.message}")
            }
        }
    }
}