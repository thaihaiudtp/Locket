package com.example.locket.model

data class LoginRequest(
    val email: String,
    val password: String
)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val token: String
)

data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String
)
data class RegisterResponse(
    val success: Boolean,
    val message: String
)