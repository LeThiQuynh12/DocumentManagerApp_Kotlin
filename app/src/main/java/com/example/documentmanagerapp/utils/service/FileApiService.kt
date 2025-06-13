package com.example.documentmanagerapp.utils.service
import com.example.documentmanagerapp.utils.data.ApiResponse
import com.example.documentmanagerapp.utils.data.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface FilesApiService {

    @Multipart
    @POST("files")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Query("folder") folder: String,
        @Query("password") password: String,
        @Query("userId") userId: Long,
        @Query("categoryId") categoryId: Long,
        @Query("documentId") documentId: Long? = null
    ): FileUploadResponse




    @Multipart
    @POST("files/multi")
    suspend fun uploadMultipleFiles(
        @Part files: List<MultipartBody.Part>,
        @Query("folder") folder: String,
        @Query("password") password: String,
        @Query("userId") userId: Long,
        @Query("categoryId") categoryId: Long,
        @Query("documentId") documentId: Long? = null
    ): ApiResponse<List<FileUploadResponse>>

    @GET("files")
    @Streaming
    suspend fun downloadFile(
        @Query("documentId") documentId: Long,
        @Query("password") password: String,
        @Query("versionNumber") versionNumber: Int? = null
    ): Response<ResponseBody>

    @GET("files/folder-size")
    suspend fun getFolderSize(
        @Query("folder") folder: String
    ): ApiResponse<Map<String, Any>>

    @GET("files/folder-sizes")
    suspend fun getFolderSizes(
        @Query("parent") parent: String
    ): ApiResponse<Map<String, Long>>

    @DELETE("files/{id}")
    suspend fun deleteMediaDocument(
        @Path("id") id: Long,
        @Query("password") password: String
    ): Response<Unit>

    @DELETE("files/link/{id}")
    suspend fun deleteLinkDocument(
        @Path("id") id: Long,
        @Query("password") password: String
    ): Response<Unit>

    @GET("files/document-size")
    suspend fun getDocumentSize(
        @Query("documentId") documentId: Long
    ): ApiResponse<Map<String, Any>>
}