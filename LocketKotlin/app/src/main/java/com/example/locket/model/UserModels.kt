package com.example.locket.model

import com.google.gson.annotations.SerializedName

data class UserSearchResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserDTO>
)
data class UserProfileResponse(
    val success: Boolean,
    val message: String,
    val data: UserDTO
)