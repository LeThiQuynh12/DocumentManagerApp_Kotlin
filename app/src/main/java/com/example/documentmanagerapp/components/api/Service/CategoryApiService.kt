package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Data.Category
import retrofit2.Call
import retrofit2.http.*

interface CategoryApiService {

    // Lấy tất cả categories
    @GET("categories")
    fun getAllCategories(): Call<List<Category>>

    // Lấy category theo ID
    @GET("categories/{id}")
    fun getCategoryById(@Path("id") id: Long): Call<Category>

    // Tạo mới category
    @POST("categories")
    fun createCategory(@Body category: Category): Call<Category>

    // Cập nhật category theo ID
    @PUT("categories/{id}")
    fun updateCategory(@Path("id") id: Long, @Body category: Category): Call<Category>

    // Xóa category theo ID
    @DELETE("categories/{id}")
    fun deleteCategory(@Path("id") id: Long): Call<Void>

    // Tìm category theo userId
    @GET("categories/user/{userId}")
    fun getCategoriesByUserId(@Path("userId") userId: Long): Call<List<Category>>
}