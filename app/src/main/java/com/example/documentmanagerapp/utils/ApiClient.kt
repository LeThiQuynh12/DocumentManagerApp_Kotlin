package com.example.documentmanagerapp.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

const val API_URL = "http://10.0.2.2:8080"

data class Tokens(
    val accessToken: String?,
    val refreshToken: String?,
    val accessExpiresAt: Long,
    val refreshExpiresAt: Long
)

data class RefreshTokenRequest(val refreshToken: String)

data class RefreshTokenResponse(
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?
)

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(
    val results: LoginResult?
)

data class LoginResult(
    val user: User?,
    @SerializedName("access_token") val accessToken: String?,
    @SerializedName("refresh_token") val refreshToken: String?,
    @SerializedName("expires_in") val expiresIn: Long?
)

data class User(
    val id: Long,
    val email: String,
    val fullName: String
)

data class UserResponse(
    val results: User?
)

interface ApiService {
    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): RefreshTokenResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("auth/account")
    suspend fun getCurrentUser(): UserResponse
}

class TokenManager(private val context: Context) {
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    suspend fun saveTokens(
        accessToken: String?,
        refreshToken: String?,
        accessExpiresIn: Long = 1000,
        refreshExpiresIn: Long = 1000
    ) {
        if (accessToken == null || refreshToken == null) {
            Log.e("TokenManager", "Cannot save tokens: accessToken or refreshToken is null")
            return
        }
        try {
            val accessExpiresAt = System.currentTimeMillis() + accessExpiresIn * 1000
            val refreshExpiresAt = System.currentTimeMillis() + refreshExpiresIn * 1000
            val tokens = Tokens(accessToken, refreshToken, accessExpiresAt, refreshExpiresAt)
            prefs.edit().putString("authTokens", Gson().toJson(tokens)).commit() // Đồng bộ
            Log.d("TokenManager", "Tokens saved: accessToken=$accessToken, expiresAt=$accessExpiresAt")
        } catch (e: Exception) {
            Log.e("TokenManager", "Error saving tokens: ${e.message}")
        }
    }

    suspend fun getTokens(): Tokens? {
        return try {
            val json = prefs.getString("authTokens", null) ?: return null
            val tokens = Gson().fromJson(json, Tokens::class.java)

            if (System.currentTimeMillis() > tokens.refreshExpiresAt) {
                Log.w("TokenManager", "Refresh token expired! Please re-login.")
                removeTokens()
                return null
            }

            if (System.currentTimeMillis() > tokens.accessExpiresAt) {
                Log.d("TokenManager", "Access token expired! Refreshing...")
                return refreshTokens(tokens.refreshToken)
            }

            tokens
        } catch (e: Exception) {
            Log.e("TokenManager", "Error retrieving tokens: ${e.message}")
            return null
        }
    }

    private suspend fun refreshTokens(refreshToken: String?): Tokens? {
        if (refreshToken == null) {
            Log.e("TokenManager", "Cannot refresh: refreshToken is null")
            return null
        }
        return try {
            val apiService = ApiClient.getClientWithoutAuth()
                .create(ApiService::class.java)
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            saveTokens(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken,
                accessExpiresIn = response.expiresIn ?: 1000
            )
            getTokens()
        } catch (e: Exception) {
            Log.e("TokenManager", "Error refreshing token: ${e.message}")
            removeTokens()
            return null
        }
    }

    suspend fun removeTokens() {
        try {
            prefs.edit().remove("authTokens").commit() // Đồng bộ
            Log.d("TokenManager", "Tokens removed")
        } catch (e: Exception) {
            Log.e("TokenManager", "Error removing tokens: ${e.message}")
        }
    }
}

object ApiClient {
    private var retrofit: Retrofit? = null

    fun getClient(context: Context): Retrofit {
        if (retrofit == null) {
            val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
            val authInterceptor = Interceptor { chain ->
                val tokenManager = TokenManager(context)
                var tokens = runBlocking { tokenManager.getTokens() }
                var request = chain.request().newBuilder()
                    .apply {
                        if (tokens?.accessToken != null) {
                            addHeader("Authorization", "Bearer ${tokens!!.accessToken}")
                        }
                    }
                    .build()

                var response = chain.proceed(request)

                if (response.code == 401) {
                    response.close()
                    tokens = runBlocking { tokenManager.getTokens() }
                    if (tokens?.accessToken != null) {
                        request = request.newBuilder()
                            .header("Authorization", "Bearer ${tokens.accessToken}")
                            .build()
                        response = chain.proceed(request)
                    }
                }

                response
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!
    }

    fun getClientWithoutAuth(): Retrofit {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(API_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}