package com.example.locket.ui.camera

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.data.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val apiService: LocketApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()

    // Cập nhật hàm nhận thêm message, time, location
    fun uploadImage(
        file: File,
        message: String? = null,
        time: String? = null,
        location: String? = null
    ) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                // 1. Chuẩn bị File
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                // 2. Chuẩn bị Text Fields (Chuyển String sang RequestBody)
                val messagePart = message?.toRequestBody("text/plain".toMediaTypeOrNull())
                val timePart = time?.toRequestBody("text/plain".toMediaTypeOrNull())
                val locationPart = location?.toRequestBody("text/plain".toMediaTypeOrNull())

                // 3. Gọi API
                val response = apiService.uploadImage(
                    file = body,
                    message = messagePart,
                    time = timePart,
                    location = locationPart
                )

                if (response.isSuccessful) {
                    _uploadState.value = UploadState.Success
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown error"
                    _uploadState.value = UploadState.Error("Lỗi upload (${response.code()}): $errorMsg")
                    Log.e("UploadImage", errorMsg)
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Lỗi mạng: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
}

sealed class UploadState {
    object Idle : UploadState()
    object Loading : UploadState()
    object Success : UploadState()
    data class Error(val message: String) : UploadState()
}