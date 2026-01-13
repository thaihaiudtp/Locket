package com.example.locket.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.data.TokenManager
import com.example.locket.model.UserDTO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = false,
    val user: UserDTO? = null,
    val friends: List<UserDTO> = emptyList(),
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val apiService: LocketApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val profileRes = apiService.getProfile()
                val friendsRes = apiService.getMyFriends()

                if (profileRes.isSuccessful && profileRes.body()?.success == true) {
                    val userData = profileRes.body()!!.data
                    val friendsList = if (friendsRes.isSuccessful && friendsRes.body()?.success == true) {
                        friendsRes.body()!!.data
                    } else {
                        emptyList()
                    }

                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            user = userData,
                            friends = friendsList
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Lỗi lấy thông tin: ${profileRes.message()}") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Lỗi mạng: ${e.message}") }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.deleteToken()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val response = apiService.deleteAccount()
                if (response.isSuccessful && response.body()?.success == true) {
                    logout()
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Không thể xóa tài khoản") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}