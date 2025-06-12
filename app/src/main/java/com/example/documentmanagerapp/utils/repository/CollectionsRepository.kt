package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.AddCategoryRequest
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.data.Document
import com.example.documentmanagerapp.utils.service.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class CollectionsRepository(private val context: Context) {
    private val apiService: CategoryApiService = ApiClient.getClient(context).create(CategoryApiService::class.java)

    suspend fun fetchData(userId: Long): Pair<List<Category>, Map<Long, Int>> =
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val categoriesResponse = apiService.getCategories(userId)
                    val documentsResponse = apiService.getDocuments(userId)

                    val categories = categoriesResponse.results ?: emptyList()
                    Log.d("CollectionsRepository", "Categories fetched: $categories")

                    val documents = documentsResponse.results ?: emptyList()
                    val documentCounts = documents.groupBy { it.category?.id ?: -1L }
                        .mapValues { it.value.size }
                        .filterKeys { it != -1L }
                    Log.d("CollectionsRepository", "Document counts: $documentCounts")

                    categories to documentCounts
                }
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error: ${e.message}")
                throw Exception("Lỗi kết nối: ${e.message}")
            }
        }

    suspend fun addCategory(name: String, group: String, userId: Long): Category? =
        withContext(Dispatchers.IO) {
            try {
                val request = AddCategoryRequest(name, group, AddCategoryRequest.User(userId))
                val response = apiService.addCategory(request)
                val newCategory = response.results
                Log.d("CollectionsRepository", "Added category: $newCategory")
                newCategory
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error adding category: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi thêm danh mục: ${e.message}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error adding category: ${e.message}")
                throw Exception("Lỗi thêm danh mục: ${e.message}")
            }
        }

    suspend fun updateCategory(categoryId: Long, name: String, group: String, userId: Long): Category =
        withContext(Dispatchers.IO) {
            try {
                val request = AddCategoryRequest(name, group, AddCategoryRequest.User(userId))
                val response = apiService.updateCategory(categoryId, request)
                val updatedCategory = response.results
                    ?: throw IllegalStateException("Không tìm thấy danh mục sau khi cập nhật")
                Log.d("CollectionsRepository", "Updated category: $updatedCategory")
                updatedCategory
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error updating category: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi sửa danh mục: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error updating category: ${e.message}")
                throw Exception("Lỗi sửa danh mục: ${e.message}")
            }
        }

    suspend fun deleteCategory(categoryId: Long, userId: Long) =
        withContext(Dispatchers.IO) {
            try {
                // Kiểm tra tài liệu liên kết
                val documents = getDocuments(userId)
                if (documents.any { it.category?.id == categoryId }) {
                    throw Exception("Danh mục có tài liệu liên kết, không thể xóa")
                }
                val response = apiService.deleteCategory(categoryId)
                Log.d("CollectionsRepository", "Delete response: $response")
                if (response.isSuccessful) {
                    val body = response.body()
                    if (response.code() == 204 || (body != null && body.status_code == 200)) {
                        Log.d("CollectionsRepository", "Deleted category: $categoryId")
                    } else {
                        throw Exception(body?.message ?: "Lỗi xóa danh mục: HTTP ${response.code()}")
                    }
                } else {
                    throw HttpException(response)
                }
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error deleting category: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Không thể xóa danh mục: HTTP ${e.code()} - ${e.message}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error deleting category: ${e.message}")
                throw e
            }
        }

    suspend fun getCategoryById(categoryId: Long): Category? =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCategoryById(categoryId)
                Log.d("CollectionsRepository", "Get category response: $response")
                if (response.status_code == 404) {
                    Log.w("CollectionsRepository", "Category not found for ID: $categoryId")
                    return@withContext null
                }
                val category = response.results
                Log.d("CollectionsRepository", "Fetched category: $category")
                category
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error fetching category: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                if (e.code() == 404) {
                    return@withContext null
                }
                throw Exception("Lỗi lấy danh mục: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error fetching category: ${e.message}")
                throw Exception("Lỗi lấy danh mục: ${e.message}")
            }
        }

    suspend fun getDocuments(userId: Long): List<Document> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDocuments(userId)
                val documents = response.results?.filterNotNull() ?: emptyList()
                Log.d("CollectionsRepository", "Fetched documents: $documents")
                documents
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error fetching documents: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: NumberFormatException) {
                Log.e("CollectionsRepository", "Invalid date format: ${e.message}")
                throw Exception("Lỗi định dạng ngày giờ: ${e.message}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error retrieving documents: ${e.message}")
                throw Exception("Lỗi lấy tài liệu: ${e.message}")
            }
        }




    suspend fun toggleFavorite(documentId: Long): Document =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.toggleFavorite(documentId)
                val updatedDocument = response.results?.results
                    ?: throw Exception("Invalid toggle favorite response")
                Log.d("CollectionsRepository", "Toggled favorite for document: $updatedDocument")
                updatedDocument
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error toggling favorite: ${e.code()} - ${e.message()}")
                if (e.code() == 401) {
                    throw Exception("Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
                }
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error toggling favorite: ${e.message}")
                throw Exception("Lỗi cập nhật trạng thái yêu thích: ${e.message}")
            }
        }
}