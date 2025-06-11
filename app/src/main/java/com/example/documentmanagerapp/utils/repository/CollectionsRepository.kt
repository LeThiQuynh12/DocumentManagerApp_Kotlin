package com.example.documentmanagerapp.utils.repository

import android.content.Context
import android.util.Log
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.data.Category
import com.example.documentmanagerapp.utils.data.AddCategoryRequest
import com.example.documentmanagerapp.utils.service.CategoryApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class CollectionsRepository(context: Context) {
    private val apiService: CategoryApiService = ApiClient.getClient(context).create(CategoryApiService::class.java)

    suspend fun fetchData(userId: Long): Pair<List<Category>, Map<Long, Int>> {
        return withContext(Dispatchers.IO) {
            try {
                val categoriesDeferred = async { apiService.getCategories(userId) }
                val documentsDeferred = async { apiService.getDocuments(userId) }

                val categoriesResponse = categoriesDeferred.await()
                val documentsResponse = documentsDeferred.await()

                val categories = categoriesResponse.results ?: emptyList()
                Log.d("CollectionsRepository", "Categories fetched: $categories")

                val documents = documentsResponse.results ?: emptyList()
                val documentCounts = documents.groupBy { it.category?.id ?: -1L }
                    .mapValues { it.value.size }
                    .filterKeys { it != -1L }
                Log.d("CollectionsRepository", "Document counts: $documentCounts")

                categories to documentCounts
            } catch (e: HttpException) {
                Log.e("CollectionsRepository", "HTTP error: ${e.code()} - ${e.message()}")
                throw Exception("Lỗi server: HTTP ${e.code()}")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error: ${e.message}")
                throw Exception("Lỗi kết nối: ${e.message}")
            }
        }
    }

    suspend fun addCategory(name: String, group: String): Category? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.addCategory(AddCategoryRequest(name, group))
                val newCategory = response.results?.firstOrNull()
                Log.d("CollectionsRepository", "Added category: $newCategory")
                newCategory
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error adding category: ${e.message}")
                throw Exception("Lỗi thêm danh mục: ${e.message}")
            }
        }
    }

    suspend fun updateCategory(categoryId: Long, name: String, group: String): Category? {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateCategory(categoryId, AddCategoryRequest(name, group))
                val updatedCategory = response.results?.firstOrNull()
                Log.d("CollectionsRepository", "Updated category: $updatedCategory")
                updatedCategory
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error updating category: ${e.message}")
                throw Exception("Lỗi sửa danh mục: ${e.message}")
            }
        }
    }

    suspend fun deleteCategory(categoryId: Long) {
        withContext(Dispatchers.IO) {
            try {
                apiService.deleteCategory(categoryId)
                Log.d("CollectionsRepository", "Deleted category: $categoryId")
            } catch (e: Exception) {
                Log.e("CollectionsRepository", "Error deleting category: ${e.message}")
                throw Exception("Lỗi xóa danh mục: ${e.message}")
            }
        }
    }
}