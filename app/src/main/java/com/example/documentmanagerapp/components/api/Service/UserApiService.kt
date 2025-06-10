package com.example.documentmanagerapp.components.api.Service

import com.example.documentmanagerapp.components.api.Data.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface UserApiService {

    @GET("users")
    fun getAllUsers(): Call<List<User>>

    @GET("users/{id}")
    fun getUserById(@Path("id") id: Long): Call<User>

    @POST("users")
    fun createUser(@Body user: User): Call<User>

    @PUT("users")
    fun updateUser(@Body user: User): Call<User>

    @DELETE("users/{id}")
    fun deleteUser(@Path("id") id: Long): Call<Long>

    @GET("users/myinfor/{email}")
    fun getUserByEmail(@Path("email") email: String): Call<User>
}
