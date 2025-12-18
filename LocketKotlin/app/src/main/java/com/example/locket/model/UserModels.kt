package com.example.locket.model

import com.google.gson.annotations.SerializedName

data class UserSearchResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserSearchData>
)

data class UserSearchData(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String
)