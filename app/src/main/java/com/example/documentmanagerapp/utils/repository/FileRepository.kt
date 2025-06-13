//package com.example.documentmanagerapp.utils.repository
//
//import android.content.Context
//import android.util.Log
//import com.example.documentmanagerapp.utils.ApiClient
//import com.example.documentmanagerapp.utils.data.FileUploadResponse
//import com.example.documentmanagerapp.utils.service.FilesApiService
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import okhttp3.MultipartBody
//import okhttp3.ResponseBody
//import retrofit2.HttpException
//
//class FileRepository(private val context: Context) {
//    private val apiService: FilesApiService =
//        ApiClient.getClient(context).create(FilesApiService::class.java)
//
//    suspend fun uploadFile(
//        file: MultipartBody.Part,
//        folder: String,
//        password: String,
//        userId: Long,
//        categoryId: Long,
//        documentId: Long? = null
//    ): FileUploadResponse = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.uploadFile(file, folder, password, userId, categoryId, documentId)
//
//            if (response.status_code != 200 || response.results == null) {
//                throw Exception(response.message)
//            }
//
//            response.results
//        } catch (e: Exception) {
//            Log.e("FileRepository", "uploadFile error: ${e.message}", e)
//            throw e
//        }
//    }
//
//
//    suspend fun uploadMultipleFiles(
//        files: List<MultipartBody.Part>,
//        folder: String,
//        password: String,
//        userId: Long,
//        categoryId: Long,
//        documentId: Long? = null
//    ): List<FileUploadResponse> = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.uploadMultipleFiles(files, folder, password, userId, categoryId, documentId)
//            response.results ?: emptyList()
//        } catch (e: Exception) {
//            Log.e("FileRepository", "uploadMultipleFiles error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun downloadFile(
//        documentId: Long,
//        password: String,
//        versionNumber: Int? = null
//    ): ByteArray = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.downloadFile(documentId, password, versionNumber)
//            if (!response.isSuccessful || response.body() == null) {
//                throw HttpException(response)
//            }
//            response.body()!!.bytes()
//        } catch (e: Exception) {
//            Log.e("FileRepository", "downloadFile error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun deleteMediaFile(id: Long, password: String) = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.deleteMediaDocument(id, password)
//            if (!response.isSuccessful) throw HttpException(response)
//        } catch (e: Exception) {
//            Log.e("FileRepository", "deleteMediaFile error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun deleteLinkFile(id: Long, password: String) = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.deleteLinkDocument(id, password)
//            if (!response.isSuccessful) throw HttpException(response)
//        } catch (e: Exception) {
//            Log.e("FileRepository", "deleteLinkFile error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun getFolderSize(folder: String): Map<String, Any> = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.getFolderSize(folder)
//            response.results ?: emptyMap()
//        } catch (e: Exception) {
//            Log.e("FileRepository", "getFolderSize error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun getFolderSizes(parent: String): Map<String, Long> = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.getFolderSizes(parent)
//            response.results ?: emptyMap()
//        } catch (e: Exception) {
//            Log.e("FileRepository", "getFolderSizes error: ${e.message}")
//            throw e
//        }
//    }
//
//    suspend fun getDocumentSize(documentId: Long): Map<String, Any> = withContext(Dispatchers.IO) {
//        try {
//            val response = apiService.getDocumentSize(documentId)
//            response.results ?: emptyMap()
//        } catch (e: Exception) {
//            Log.e("FileRepository", "getDocumentSize error: ${e.message}")
//            throw e
//        }
//    }
//}
package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.FileUploadResponse
import com.example.documentmanagerapp.utils.service.FilesApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.HttpException

class FileRepository(private val context: Context) {
    private val apiService: FilesApiService =
        ApiClient.getClient(context).create(FilesApiService::class.java)

    suspend fun uploadFile(
        file: MultipartBody.Part,
        folder: String,
        password: String,
        userId: Long,
        categoryId: Long,
        documentId: Long? = null
    ): FileUploadResponse = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadFile(file, folder, password, userId, categoryId, documentId)
            Log.d("FileRepository", "API Response: $response")

            // Kiểm tra tối thiểu
            if (response.s3Url.isBlank()) {
                throw Exception("Upload failed: Empty s3Url")
            }

            response
        } catch (e: Exception) {
            Log.e("FileRepository", "uploadFile error: ${e.message}", e)
            throw e
        }
    }



    suspend fun uploadMultipleFiles(
        files: List<MultipartBody.Part>,
        folder: String,
        password: String,
        userId: Long,
        categoryId: Long,
        documentId: Long? = null
    ): List<FileUploadResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.uploadMultipleFiles(files, folder, password, userId, categoryId, documentId)
            if (response.status_code != 200 || response.results == null) {
                Log.e("FileRepository", "Upload multiple files failed: ${response.message}")
                throw Exception("Lỗi tải nhiều tệp: ${response.message}")
            }
            Log.d("FileRepository", "Uploaded multiple files: ${response.results}")
            response.results
        } catch (e: HttpException) {
            Log.e("FileRepository", "HTTP error uploading multiple files: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
            }
            throw Exception("Lỗi tải nhiều tệp: HTTP ${e.code()}")
        } catch (e: Exception) {
            Log.e("FileRepository", "uploadMultipleFiles error: ${e.message}")
            throw Exception("Lỗi tải nhiều tệp: ${e.message}")
        }
    }

    suspend fun downloadFile(
        documentId: Long,
        password: String,
        versionNumber: Int? = null
    ): ByteArray = withContext(Dispatchers.IO) {
        try {
            val response = apiService.downloadFile(documentId, password, versionNumber)
            if (!response.isSuccessful || response.body() == null) {
                Log.e("FileRepository", "Download file failed: HTTP ${response.code()}")
                throw HttpException(response)
            }
            Log.d("FileRepository", "Downloaded file for document $documentId")
            response.body()!!.bytes()
        } catch (e: HttpException) {
            Log.e("FileRepository", "HTTP error downloading file: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
            }
            throw Exception("Lỗi tải tệp về: HTTP ${e.code()}")
        } catch (e: Exception) {
            Log.e("FileRepository", "downloadFile error: ${e.message}")
            throw Exception("Lỗi tải tệp về: ${e.message}")
        }
    }

    suspend fun deleteMediaFile(id: Long, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteMediaDocument(id, password)
            if (!response.isSuccessful) {
                Log.e("FileRepository", "Delete media file failed: HTTP ${response.code()}")
                throw HttpException(response)
            }
            Log.d("FileRepository", "Deleted media file $id")
        } catch (e: HttpException) {
            Log.e("FileRepository", "HTTP error deleting media file: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
            }
            throw Exception("Lỗi xóa tệp phương tiện: HTTP ${e.code()}")
        } catch (e: Exception) {
            Log.e("FileRepository", "deleteMediaFile error: ${e.message}")
            throw Exception("Lỗi xóa tệp phương tiện: ${e.message}")
        }
    }

    suspend fun deleteLinkFile(id: Long, password: String) = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteLinkDocument(id, password)
            if (!response.isSuccessful) {
                Log.e("FileRepository", "Delete link file failed: HTTP ${response.code()}")
                throw HttpException(response)
            }
            Log.d("FileRepository", "Deleted link file $id")
        } catch (e: HttpException) {
            Log.e("FileRepository", "HTTP error deleting link file: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
            }
            throw Exception("Lỗi xóa liên kết tệp: HTTP ${e.code()}")
        } catch (e: Exception) {
            Log.e("FileRepository", "deleteLinkFile error: ${e.message}")
            throw Exception("Lỗi xóa liên kết tệp: ${e.message}")
        }
    }

    suspend fun getFolderSize(folder: String): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFolderSize(folder)
            if (response.status_code != 200 || response.results == null) {
                Log.e("FileRepository", "Get folder size failed: ${response.message}")
                throw Exception("Lỗi lấy dung lượng thư mục: ${response.message}")
            }
            Log.d("FileRepository", "Folder size: ${response.results}")
            response.results
        } catch (e: Exception) {
            Log.e("FileRepository", "getFolderSize error: ${e.message}")
            throw Exception("Lỗi lấy dung lượng thư mục: ${e.message}")
        }
    }

    suspend fun getFolderSizes(parent: String): Map<String, Long> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getFolderSizes(parent)
            if (response.status_code != 200 || response.results == null) {
                Log.e("FileRepository", "Get folder sizes failed: ${response.message}")
                throw Exception("Lỗi lấy danh sách dung lượng thư mục: ${response.message}")
            }
            Log.d("FileRepository", "Folder sizes: ${response.results}")
            response.results
        } catch (e: Exception) {
            Log.e("FileRepository", "getFolderSizes error: ${e.message}")
            throw Exception("Lỗi lấy danh sách dung lượng thư mục: ${e.message}")
        }
    }

    suspend fun getDocumentSize(documentId: Long): Map<String, Any> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocumentSize(documentId)
            if (response.status_code != 200 || response.results == null) {
                Log.e("FileRepository", "Get document size failed: ${response.message}")
                throw Exception("Lỗi lấy dung lượng tài liệu: ${response.message}")
            }
            Log.d("FileRepository", "Document size: ${response.results}")
            response.results
        } catch (e: Exception) {
            Log.e("FileRepository", "getDocumentSize error: ${e.message}")
            throw Exception("Lỗi lấy dung lượng tài liệu: ${e.message}")
        }
    }
}
