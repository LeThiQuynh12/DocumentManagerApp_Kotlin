package com.example.documentmanagerapp.utils.repository


import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.PermissionData
import com.example.documentmanagerapp.utils.data.ShareRequestData
import com.example.documentmanagerapp.utils.data.UpdatePermissionRequestData
import com.example.documentmanagerapp.utils.service.PermissionApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PermissionRepository(context: Context) {

    private val apiService: PermissionApiService =
        ApiClient.getClient(context).create(PermissionApiService::class.java)

    suspend fun createPermission(permission: PermissionData): PermissionData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.createPermission(permission)
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error creating permission: ${e.message}")
                throw Exception("Lỗi tạo quyền truy cập: ${e.message}")
            }
        }
    }

    suspend fun shareDocumentByEmail(request: ShareRequestData): PermissionData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.shareDocumentByEmail(request)
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error sharing document: ${e.message}")
                throw Exception("Lỗi chia sẻ tài liệu qua email: ${e.message}")
            }
        }
    }

    suspend fun getPermissionsByUserId(userId: Long): List<PermissionData> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getPermissionsByUserId(userId)
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error fetching permissions by user: ${e.message}")
                throw Exception("Lỗi lấy quyền người dùng: ${e.message}")
            }
        }
    }

    suspend fun getPermissionsByDocumentId(docId: Long): List<PermissionData> {
        return withContext(Dispatchers.IO) {
            try {
                apiService.getPermissionsByDocumentId(docId)
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error fetching permissions by document: ${e.message}")
                throw Exception("Lỗi lấy quyền theo tài liệu: ${e.message}")
            }
        }
    }

    suspend fun updatePermission(request: UpdatePermissionRequestData): PermissionData? {
        return withContext(Dispatchers.IO) {
            try {
                apiService.updatePermissionByUserAndDoc(request)
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error updating permission: ${e.message}")
                throw Exception("Lỗi cập nhật quyền: ${e.message}")
            }
        }
    }

    suspend fun deletePermission(permissionId: Long) {
        withContext(Dispatchers.IO) {
            try {
                apiService.deletePermission(permissionId)
                Log.d("PermissionRepository", "Deleted permission ID: $permissionId")
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error deleting permission: ${e.message}")
                throw Exception("Lỗi xóa quyền: ${e.message}")
            }
        }
    }
}
