package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.ApiResponse
import com.example.documentmanagerapp.utils.data.DocumentVersionData
import com.example.documentmanagerapp.utils.data.DocumentVersionsResponse
import retrofit2.http.*

interface DocumentVersionApiService {

    // Lấy tất cả phiên bản của một document theo docId
    @GET("document-versions/document/{docId}")
    suspend fun getVersionsByDocumentId(
        @Path("docId") docId: Long
    ): DocumentVersionsResponse

    // Tạo một phiên bản mới của tài liệu
    @POST("document-versions")
    suspend fun createDocumentVersion(
        @Body version: DocumentVersionData
    ): DocumentVersionData

    // Lấy danh sách phiên bản theo documentId (endpoint khác)
    @GET("document-versions/{documentId}/versions")
    suspend fun getVersions(
        @Path("documentId") documentId: Long
    ): List<DocumentVersionData>

    @GET("document-versions/{versionId}")
    suspend fun getVersionById(
        @Path("versionId") versionId: Long
    ): ApiResponse<DocumentVersionData>
}