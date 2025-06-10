package com.example.documentmanagerapp.components.api

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecureStorage {
    private const val PREFS_FILE = "secure_prefs"
    private const val TAG = "SecureStorage"
    private var encryptedPrefs: SharedPreferences? = null

    fun init(context: Context) {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        encryptedPrefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) {
        encryptedPrefs?.edit()?.putString("auth_token", token)?.apply()
            ?: Log.w(TAG, "EncryptedSharedPreferences chưa được khởi tạo. Gọi init() trước.")
    }

    fun getToken(): String? {
        return encryptedPrefs?.getString("auth_token", null).also {
            if (encryptedPrefs == null) {
                Log.w(TAG, "EncryptedSharedPreferences chưa được khởi tạo. Gọi init() trước.")
            }
        }
    }

    fun clearToken() {
        encryptedPrefs?.edit()?.clear()?.apply()
            ?: Log.w(TAG, "EncryptedSharedPreferences chưa được khởi tạo. Gọi init() trước.")
    }
}
