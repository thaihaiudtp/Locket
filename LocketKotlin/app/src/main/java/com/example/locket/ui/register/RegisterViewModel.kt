package com.example.locket.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.LocketApiService
import com.example.locket.model.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val apiService: LocketApiService
) : ViewModel(){
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState = _registerState.asStateFlow()
    fun register(email: String, username:String, password:String){
        viewModelScope.launch {
            _registerState.value = RegisterState.Loading
            try {
                val response = apiService.register(RegisterRequest(email, username, password))
                val body = response.body()
                if(response.isSuccessful && body != null){
                    if(body.success){
                        _registerState.value = RegisterState.Success
                    } else {
                        _registerState.value = RegisterState.Error(body.message)
                    }
                } else {
                    _registerState.value = RegisterState.Error("Lỗi server: ${response.code()}")
                }
            } catch (e: Exception) {
                _registerState.value = RegisterState.Error("Lỗi mạng: ${e.message}")
            }
        }
    }
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    object Success : RegisterState()
    data class Error(val message: String) : RegisterState()
}