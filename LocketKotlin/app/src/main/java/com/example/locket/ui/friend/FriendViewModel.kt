package com.example.locket.ui.friend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.FriendRequestActionRequest
import com.example.locket.model.FriendRequestReceivedItem
import com.example.locket.model.FriendRequestSentItem
import com.example.locket.model.SendFriendRequestRequest
import com.example.locket.model.UserDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FriendScreenState(
    val isLoading: Boolean = false,
    val searchResults: List<UserDTO> = emptyList(),
    val receivedRequests: List<FriendRequestReceivedItem> = emptyList(),
    val sentRequests: List<FriendRequestSentItem> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FriendViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendScreenState())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        // Tải danh sách lời mời ngay khi vào màn hình
        refreshRequests()
    }

    // Tải danh sách Received và Sent
    private fun refreshRequests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Gọi song song 2 API để tiết kiệm thời gian
                val receivedDeferred = apiService.getReceivedFriendRequests()
                val sentDeferred = apiService.getSentFriendRequests()

                val receivedBody = receivedDeferred.body()
                val sentBody = sentDeferred.body()

                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        receivedRequests = if (receivedDeferred.isSuccessful && receivedBody?.success == true) receivedBody.data else emptyList(),
                        sentRequests = if (sentDeferred.isSuccessful && sentBody?.success == true) sentBody.data else emptyList()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Lỗi tải dữ liệu: ${e.message}") }
            }
        }
    }

    // Tìm kiếm User (Debounce)
    fun searchUser(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList()) }
            return
        }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.searchUsers(query)
                val body = response.body()
                if (response.isSuccessful && body != null && body.success) {
                    _uiState.update { it.copy(isLoading = false, searchResults = body.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, searchResults = emptyList()) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    // Gửi lời mời kết bạn
    fun sendFriendRequest(user: UserDTO) {
        viewModelScope.launch {
            try {
                val response = apiService.sendFriendRequest(SendFriendRequestRequest(receiverId = user.id))
                if (response.isSuccessful && response.body()?.success == true) {
                    // Update UI tạm thời để user thấy phản hồi ngay
                    // 1. Cập nhật trạng thái trong list search
                    val updatedSearch = _uiState.value.searchResults.map {
                        if (it.id == user.id) it.copy(relationshipStatus = "sent") else it
                    }
                    _uiState.update { it.copy(searchResults = updatedSearch) }

                    // 2. Load lại list Sent để đồng bộ
                    refreshRequests()
                } else {
                    _uiState.update { it.copy(error = "Gửi thất bại: ${response.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Lỗi mạng: ${e.message}") }
            }
        }
    }

    // Chấp nhận kết bạn
    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.acceptFriendRequest(FriendRequestActionRequest(requestId))
                if (response.isSuccessful) {
                    refreshRequests() // Load lại list để xóa item đã accept
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Từ chối (Xóa) lời mời
    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.rejectFriendRequest(FriendRequestActionRequest(requestId))
                if (response.isSuccessful) {
                    refreshRequests()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}