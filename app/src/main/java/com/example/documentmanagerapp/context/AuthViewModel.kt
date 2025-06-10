package com.example.documentmanagerapp.components.context

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

    private val _loading = MutableLiveData(true)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Thêm biến để kiểm tra trạng thái thái đăng xuất
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
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error loading user: ${e.message}")
            _error.postValue("Lỗi tải thông tin người dùng: ${e.message}")
        }
    }

    suspend fun login(username: String, password: String): Boolean {
        _loading.postValue(true)
        try {
            Log.d("AuthViewModel", "Login attempt with username: $username, password: ****")
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
                accessExpiresIn = result.expiresIn ?: 3600 // Mặc định 1 giờ
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
                400 -> "Đăng nhập thất bại: Thông tin không hợp lệ (HTTP 400)"
                401 -> "Đăng nhập thất bại: Email hoặc mật khẩu sai"
                else -> "Đăng nhập thất bại: ${e.message()}"
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
            prefs.edit().putString("user", Gson().toJson(user)).commit() // Sử dụng commit để đồng bộ
            _user.postValue(user)
            Log.d("AuthViewModel", "Saved user to storage: $user")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error saving user: ${e.message}")
            _error.postValue("Lỗi lưu thông tin người dùng: ${e.message}")
        }
    }

    suspend fun getCurrentUser(forceUpdate: Boolean = false) {
        if (!forceUpdate) {
            loadUserFromStorage()
            _loading.postValue(false)
            return
        }
        val tokens = tokenManager.getTokens()
        if (tokens?.accessToken == null) {
            Log.w("AuthViewModel", "No access token found, cannot fetch user")
            _error.postValue("Không tìm thấy token đăng nhập")
            _loading.postValue(false)
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
                _error.postValue("Không nhận được dữ liệu người dùng từ server")
                _user.postValue(null)
                prefs.edit().remove("user").commit() // Đồng bộ
            }
        } catch (e: HttpException) {
            Log.e("AuthViewModel", "HTTP error fetching user: ${e.code()} - ${e.message()}")
            _error.postValue("Lỗi lấy thông tin người dùng: HTTP ${e.code()}")
            _user.postValue(null)
            prefs.edit().remove("user").commit()
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error fetching user: ${e.message}")
            _error.postValue("Lỗi lấy thông tin người dùng: ${e.message}")
            _user.postValue(null)
            prefs.edit().remove("user").commit()
        } finally {
            _loading.postValue(false)
        }
    }

    suspend fun logout() {
        try {
            // Xóa user và token đồng bộ
            prefs.edit().remove("user").commit()
            tokenManager.removeTokens()
            _user.postValue(null)
            isLoggedOut = true // Đánh dấu trạng thái đăng xuất
            Log.i("AuthViewModel", "Logout successful")
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Error during logout: ${e.message}")
            _error.postValue("Lỗi đăng xuất: ${e.message}")
        }
    }
}