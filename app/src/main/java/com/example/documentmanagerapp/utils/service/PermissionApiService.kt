package com.example.documentmanagerapp.utils.service


import com.example.documentmanagerapp.utils.data.PermissionData
import com.example.documentmanagerapp.utils.data.ShareRequestData
import com.example.documentmanagerapp.utils.data.UpdatePermissionRequestData
import retrofit2.http.*

interface PermissionApiService {

    // Tạo quyền phân quyền tài liệu
    @POST("permissions")
    suspend fun createPermission(
        @Body permission: PermissionData
    ): PermissionData

    // Thêm quyền cho user bằng email
    @POST("permissions/email")
    suspend fun shareDocumentByEmail(
        @Body request: ShareRequestData
    ): PermissionData

    // Lấy quyền của người dùng theo userId
    @GET("permissions/user/{userId}")
    suspend fun getPermissionsByUserId(
        @Path("userId") userId: Long
    ): List<PermissionData>

    // Lấy quyền của tài liệu theo docId
    @GET("permissions/document/{docId}")
    suspend fun getPermissionsByDocumentId(
        @Path("docId") docId: Long
    ): List<PermissionData>

    // Sửa quyền của tài liệu cho người dùng
    @PUT("permissions")
    suspend fun updatePermissionByUserAndDoc(
        @Body request: UpdatePermissionRequestData
    ): PermissionData

    // Xóa quyền theo id
    @DELETE("permissions/{id}")
    suspend fun deletePermission(
        @Path("id") id: Long
    )
}
