package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Data.response.FileUploadResponse
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface FileApiService {

    // Tải lên một file
    @Multipart
    @POST("files")
    fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("folder") folder: String,
        @Part("password") password: String,
        @Part("userId") userId: Long,
        @Part("categoryId") categoryId: Long,
        @Part("documentId") documentId: Long? = null
    ): Call<FileUploadResponse>

    // Tải lên nhiều file
    @Multipart
    @POST("files/multi")
    fun uploadMultipleFiles(
        @Part files: List<MultipartBody.Part>,
        @Part("folder") folder: String,
        @Part("password") password: String,
        @Part("userId") userId: Long,
        @Part("categoryId") categoryId: Long,
        @Part("documentId") documentId: Long? = null
    ): Call<List<FileUploadResponse>>

    // Tải file về (version có thể null)
    @GET("files")
    fun downloadFile(
        @Query("documentId") documentId: Long,
        @Query("password") password: String,
        @Query("versionNumber") versionNumber: Int? = null
    ): Call<ResponseBody>

    // Xoá file thực (media) trên S3 và CSDL
    @DELETE("files/{id}")
    fun deleteMediaDocument(
        @Path("id") id: Long,
        @Query("password") password: String
    ): Call<Void>

    // Xoá link (reference) trong CSDL
    @DELETE("files/link/{id}")
    fun deleteLinkDocument(
        @Path("id") id: Long,
        @Query("password") password: String
    ): Call<Void>

    // Dung lượng của 1 folder
    @GET("files/folder-size")
    fun getFolderSize(
        @Query("folder") folder: String
    ): Call<Map<String, Any>>

    // Dung lượng của các folder con trong thư mục cha
    @GET("files/folder-sizes")
    fun getFolderSizes(
        @Query("parent") parent: String
    ): Call<Map<String, Long>>

    // Tổng dung lượng của một document theo tất cả các version
    @GET("files/document-size")
    fun getDocumentSize(
        @Query("documentId") documentId: Long
    ): Call<Map<String, Any>>
}