package com.example.locket.data

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

// Tạo DataStore (chỉ 1 instance duy nhất)
private val Context.dataStore by preferencesDataStore("locket_prefs")

@Singleton
class TokenManager @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("token")
    }

    // 1. Flow lấy Token thô
    val tokenFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // 2. Flow lấy User ID (Tự động giải mã từ Token)
    val userIdFlow: Flow<String?> = tokenFlow.map { token ->
        if (token != null) decodePayload(token, "id") else null
    }

    // 3. (Bonus) Flow lấy Username nếu cần
    val usernameFlow: Flow<String?> = tokenFlow.map { token ->
        if (token != null) decodePayload(token, "username") else null
    }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }

    // Hàm giải mã JWT (Decode Base64)
    private fun decodePayload(token: String, key: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                // Payload là phần thứ 2 trong chuỗi JWT
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val jsonObject = JSONObject(payload)

                // Lấy giá trị theo key ("id", "username", "email"...)
                return jsonObject.optString(key)
            }
        } catch (e: Exception) {
            Log.e("TokenManager", "Error decoding token", e)
        }
        return null
    }
}