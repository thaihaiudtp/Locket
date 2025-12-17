package com.example.locket.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.PictureDetailData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel() {

    private val _detailState = MutableStateFlow<DetailState>(DetailState.Loading)
    val detailState = _detailState.asStateFlow()

    fun loadPictureDetail(id: String) {
        viewModelScope.launch {
            _detailState.value = DetailState.Loading
            try {
                val response = apiService.getPictureDetail(id)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    _detailState.value = DetailState.Success(body.data)
                } else {
                    _detailState.value = DetailState.Error("Lỗi: ${response.code()}")
                }
            } catch (e: Exception) {
                _detailState.value = DetailState.Error("Lỗi mạng: ${e.message}")
            }
        }
    }

    // Hàm tiện ích chuyển đổi thời gian (VD: 2025-12-17T... -> "5 mins ago")
    fun calculateTimeAgo(isoString: String): String {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            format.timeZone = TimeZone.getTimeZone("UTC")
            val date = format.parse(isoString) ?: return "Just now"

            val diff = System.currentTimeMillis() - date.time
            val minutes = diff / (1000 * 60)
            val hours = minutes / 60
            val days = hours / 24

            when {
                minutes < 1 -> "Just now"
                minutes < 60 -> "$minutes m ago"
                hours < 24 -> "$hours h ago"
                else -> "$days d ago"
            }
        } catch (e: Exception) {
            "Just now"
        }
    }
}

sealed class DetailState {
    object Loading : DetailState()
    data class Success(val data: PictureDetailData) : DetailState()
    data class Error(val message: String) : DetailState()
}