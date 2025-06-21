package com.example.documentmanagerapp.utils.service

import com.example.documentmanagerapp.utils.data.PermissionData
import com.example.documentmanagerapp.utils.data.PermissionsResponse
import com.example.documentmanagerapp.utils.data.ShareRequestData
import com.example.documentmanagerapp.utils.data.UpdatePermissionRequestData
import retrofit2.Response
import retrofit2.http.*

interface PermissionApiService {

    @POST("permissions")
    suspend fun createPermission(
        @Body permission: PermissionData
    ): PermissionData

    @POST("permissions/email")
    suspend fun shareDocumentByEmail(
        @Body request: ShareRequestData
    ): PermissionData

    @GET("permissions/user/{userId}")
    suspend fun getPermissionsByUserId(
        @Path("userId") userId: Long
    ): List<PermissionData>

    @GET("permissions/document/{docId}")
    suspend fun getPermissionsByDocumentId(
        @Path("docId") docId: Long
    ): PermissionsResponse // Sửa đổi để trả về PermissionsResponse

    @PUT("permissions")
    suspend fun updatePermissionByUserAndDoc(
        @Body request: UpdatePermissionRequestData
    ): PermissionData


    @DELETE("permissions/{id}")
    suspend fun deletePermission(
        @Path("id") id: Long): Response<Unit>
}