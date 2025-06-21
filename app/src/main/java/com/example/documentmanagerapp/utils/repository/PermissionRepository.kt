package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.PermissionData
import com.example.documentmanagerapp.utils.data.PermissionsResponse
import com.example.documentmanagerapp.utils.data.ShareRequestData
import com.example.documentmanagerapp.utils.data.UpdatePermissionRequestData
import com.example.documentmanagerapp.utils.service.PermissionApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

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
            } catch (e: HttpException) {
                val errorMessage = when {
                    e.code() == 404 -> "Người dùng với email ${request.email} không tồn tại"
                    e.code() == 400 -> {
                        val responseBody = e.response()?.errorBody()?.string()
                        if (responseBody?.contains("user not found", ignoreCase = true) == true) {
                            "Người dùng với email ${request.email} không tồn tại"
                        } else {
                            "Lỗi chia sẻ tài liệu: Yêu cầu không hợp lệ (${e.message})"
                        }
                    }
                    else -> "Lỗi chia sẻ tài liệu qua email: ${e.message}"
                }
                Log.e("PermissionRepository", "Error sharing document: ${e.message}")
                throw Exception(errorMessage)
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
                val response = apiService.getPermissionsByDocumentId(docId)
                response.permissions ?: emptyList()
            } catch (e: HttpException) {
                if (e.code() == 404) {
                    Log.d("PermissionRepository", "No permissions found for document ID: $docId")
                    emptyList() // Trả về danh sách rỗng thay vì ném lỗi
                } else {
                    Log.e("PermissionRepository", "Error fetching permissions by document: ${e.message}")
                    throw Exception("Lỗi lấy quyền theo tài liệu: ${e.message}")
                }
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
                val response = apiService.deletePermission(permissionId)
                if (response.isSuccessful) {
                    Log.d("PermissionRepository", "Deleted permission $permissionId successfully, code: ${response.code()}")
                } else {
                    Log.e("PermissionRepository", "Failed to delete permission, code: ${response.code()}, message: ${response.message()}")
                    throw Exception("Failed to delete permission, code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("PermissionRepository", "Error deleting permission: ${e.message}")
                throw Exception("Lỗi xóa quyền: ${e.message}")
            }
        }
    }
}