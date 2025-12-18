package com.example.locket.ui.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.UserSearchData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel() {

    private val _friendState = MutableStateFlow<FriendState>(FriendState.Idle)
    val friendState = _friendState.asStateFlow()

    private var searchJob: Job? = null

    fun searchUser(query: String) {
        if (query.isBlank()) {
            _friendState.value = FriendState.Idle
            return
        }

        // Hủy job cũ nếu người dùng gõ tiếp (Debounce đơn giản)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500) // Đợi 500ms sau khi ngừng gõ mới gọi API
            _friendState.value = FriendState.Loading
            try {
                val response = apiService.searchUsers(query)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    _friendState.value = FriendState.Success(body.data)
                } else {
                    _friendState.value = FriendState.Error("Không tìm thấy user")
                }
            } catch (e: Exception) {
                _friendState.value = FriendState.Error("Lỗi mạng: ${e.message}")
            }
        }
    }
}

sealed class FriendState {
    object Idle : FriendState()
    object Loading : FriendState()
    data class Success(val users: List<UserSearchData>) : FriendState()
    data class Error(val message: String) : FriendState()
}