package com.example.locket.ui.camera

import androidx.compose.runtime.MutableState
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
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val apiService: LocketApiService,
    private val tokenManager: TokenManager
) : ViewModel(){
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState = _uploadState.asStateFlow()
    fun uploadImage(file: File) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading
            try {
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
                val response = apiService.uploadImage(body)
                if(response.isSuccessful) {
                    _uploadState.value = UploadState.Success
                } else {
                    _uploadState.value = UploadState.Error("Lỗi upload: ${response.code()}, ${response.errorBody()}")
                    Log.e(
                        "Uploadimage",
                        "${response.errorBody()}"
                    )
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Lỗi mạng: ${e.message}")
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