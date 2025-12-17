package com.example.locket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.locket.data.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenManager: TokenManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow("login")
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val token = tokenManager.tokenFlow.first()
            if(!token.isNullOrBlank()) {
                _startDestination.value = "camera"
            } else {
                _startDestination.value = "login"
            }
            delay(500)
            _isLoading.value = false
        }
    }
}