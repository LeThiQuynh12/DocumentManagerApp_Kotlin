package com.example.documentmanagerapp.context

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.documentmanagerapp.utils.ApiClient
import com.example.documentmanagerapp.utils.ApiService
import com.example.documentmanagerapp.utils.User
import com.example.documentmanagerapp.utils.LoginRequest
import com.example.documentmanagerapp.utils.RegisterRequest
import com.example.documentmanagerapp.utils.TokenManager
import com.google.gson.Gson
import kotlinx.coroutines.launch
import retrofit2.HttpException

class AuthViewModel(private val context: Context) : ViewModel() {
    private val apiService = ApiClient.getClient(context).create(ApiService::class.java)
    private val tokenManager = TokenManager(context)
    private val prefs = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _user = MutableLiveData<User?>(null)
    val user: LiveData<User?> = _user

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var isLoggedOut = false

    init {
        viewModelScope.launch {
            if (!isLoggedOut) {
                loadUserFromStorage()
            }
            _loading.postValue(false)
        }
    }

    private suspend fun loadUserFromStorage() {
        try {
            val userJson = prefs.getString("user", null)
            if (userJson != null) {
                val user = Gson().fromJson(userJson, User::class.java)
                _user.postValue(user)
                Log.d("AuthViewModel", "User loaded from storage: $user")
            } else {
                Log.d("AuthViewModel", "No user found in storage")
                _user.postValue(null)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error loading user: ${e.message}")
            // Không post error để tránh thông báo không cần thiết
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        _loading.postValue(true)
        try {
            Log.d("AuthViewModel", "Login attempt with username: $username")
            val response = apiService.login(LoginRequest(username, password))
            Log.d("AuthViewModel", "Login API response: $response")
            val result = response.results
            if (result?.accessToken == null || result?.refreshToken == null) {
                Log.e("AuthViewModel", "Login failed: accessToken or refreshToken is null")
                _error.postValue("Đăng nhập thất bại: Token không hợp lệ")
                return false
            }
            tokenManager.saveTokens(
                accessToken = result.accessToken,
                refreshToken = result.refreshToken,
                accessExpiresIn = result.expiresIn ?: 3600
            )
            result.user?.let { user ->
                saveUserToStorage(user)
                Log.i("AuthViewModel", "User saved after login: $user")
            } ?: run {
                Log.w("AuthViewModel", "No user data in login response, calling getCurrentUser")
                getCurrentUser(true)
            }
            _error.postValue(null)
            isLoggedOut = false
            return true
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Thông tin đăng nhập không hợp lệ"
                401 -> "Email hoặc mật khẩu sai"
                else -> "Lỗi đăng nhập: HTTP ${e.code()}"
            }
            Log.e("AuthViewModel", "Login failed: HTTP ${e.code()} - ${e.message()}")
            _error.postValue(errorMessage)
            return false
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Login failed: ${e.message}")
            _error.postValue("Lỗi đăng nhập: ${e.message}")
            return false
        } finally {
            _loading.postValue(false)
        }
    }

    suspend fun saveUserToStorage(user: User) {
        try {
            prefs.edit().putString("user", Gson().toJson(user)).apply() // Sử dụng apply() thay vì commit()
            _user.postValue(user)
            Log.d("AuthViewModel", "Saved user to storage: $user")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving user: ${e.message}")
            // Không post error để tránh thông báo không cần thiết
        }
    }

    suspend fun getCurrentUser(forceUpdate: Boolean = false) {
        if (!forceUpdate) {
            loadUserFromStorage()
            return
        }
        val tokens = tokenManager.getTokens()
        if (tokens?.accessToken == null) {
            Log.w("AuthViewModel", "No access token found, cannot fetch user")
            _user.postValue(null)
            return
        }
        try {
            Log.d("AuthViewModel", "Fetching current user from API")
            val response = apiService.getCurrentUser()
            if (response.results != null) {
                saveUserToStorage(response.results)
                Log.i("AuthViewModel", "User fetched successfully: ${response.results}")
            } else {
                Log.e("AuthViewModel", "API returned null user data")
                _user.postValue(null)
                prefs.edit().remove("user").apply()
            }
        } catch (e: HttpException) {
            Log.e("AuthViewModel", "HTTP error fetching user: ${e.code()} - ${e.message()}")
            if (e.code() == 401) {
                tokenManager.removeTokens()
                _user.postValue(null)
                prefs.edit().remove("user").apply()
            }
            // Không post error để tránh thông báo không cần thiết
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user: ${e.message}")
            _user.postValue(null)
            prefs.edit().remove("user").apply()
        } finally {
            _loading.postValue(false)
        }
    }

    suspend fun register(fullName: String, email: String, password: String): Boolean {
        _loading.postValue(true)
        try {
            Log.d("AuthViewModel", "Register attempt with email: $email, fullName: $fullName")
            val response = apiService.register(RegisterRequest(fullName, email, password))
            Log.d("AuthViewModel", "Register API response: $response")
            response.results?.let { user ->
                _user.postValue(user)
                Log.i("AuthViewModel", "User registered: $user")
                _error.postValue(null)
                return true
            } ?: run {
                Log.e("AuthViewModel", "Registration failed: No user data in response")
                _error.postValue("Đăng ký thất bại: Không nhận được dữ liệu người dùng")
                return false
            }
        } catch (e: HttpException) {
            val errorMessage = when (e.code()) {
                400 -> "Thông tin đăng ký không hợp lệ"
                409 -> "Email đã tồn tại"
                else -> "Lỗi đăng ký: HTTP ${e.code()}"
            }
            Log.e("AuthViewModel", "Register failed: HTTP ${e.code()} - ${e.message()}")
            _error.postValue(errorMessage)
            return false
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Register failed: ${e.message}")
            _error.postValue("Lỗi đăng ký: ${e.message}")
            return false
        } finally {
            _loading.postValue(false)
        }
    }

    suspend fun logout() {
        try {
            prefs.edit().remove("user").apply()
            tokenManager.removeTokens()
            _user.postValue(null)
            isLoggedOut = true
            Log.i("AuthViewModel", "Logout successful")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error during logout: ${e.message}")
            _error.postValue("Lỗi đăng xuất: ${e.message}")
        }
    }
}