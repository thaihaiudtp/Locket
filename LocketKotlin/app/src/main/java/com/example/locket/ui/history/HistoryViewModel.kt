package com.example.locket.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.PictureData
import com.example.locket.model.SendMessageRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel(){
    private val _pictures = MutableStateFlow<List<PictureData>>(emptyList())
    val pictures = _pictures.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var currentPage = 1
    private var totalPages = 1
    private val pageSize = 10

    init {
        loadNextPage() // Tải trang 1 ngay khi khởi tạo
    }

    fun loadNextPage() {
        if (_isLoading.value || currentPage > totalPages) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPictureList(page = currentPage, limit = pageSize)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    totalPages = body.meta.totalPages
                    val currentList = _pictures.value.toMutableList()
                    currentList.addAll(body.data)
                    _pictures.value = currentList
                    currentPage++
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun sendMessage(receiverId: String, content: String, pictureId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val request = SendMessageRequest(
                    receiverId = receiverId,
                    content = content,
                    attachedPictureId = pictureId // Gắn ID ảnh để biết đang reply ảnh nào
                )
                val response = apiService.sendMessage(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}