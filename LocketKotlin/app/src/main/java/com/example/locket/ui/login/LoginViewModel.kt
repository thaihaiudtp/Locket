package com.example.locket.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.data.TokenManager
import com.example.locket.model.LoginRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val apiService: LocketApiService,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    // Đổi tham số từ username -> email
    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                // Sử dụng LoginRequest mới với email
                val response = apiService.login(LoginRequest(email = email, password = pass))
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    if (body.success) {
                        // Lưu token và báo thành công
                        tokenManager.saveToken(body.token)
                        _loginState.value = LoginState.Success
                    } else {
                        // API trả về 200 nhưng logic báo lỗi (ví dụ: sai pass)
                        _loginState.value = LoginState.Error(body.message)
                    }
                } else {
                    _loginState.value = LoginState.Error("Lỗi server: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error("Lỗi mạng: ${e.message}")
            }
        }
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}