package com.example.locket.model

import com.google.gson.annotations.SerializedName

data class UserDTO(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String,
    val relationshipStatus: String? = null
)
data class BaseResponse(
    val success: Boolean,
    val message: String
)

data class FriendListResponse(
    val success: Boolean,
    val message: String,
    val data: List<UserDTO>
)

data class FriendRequestReceivedResponse(
    val success: Boolean,
    val message: String,
    val data: List<FriendRequestReceivedItem>
)
data class FriendRequestReceivedItem(
    @SerializedName("_id") val id: String,
    // Ở API nhận: senderId được populate -> trả về Object User
    @SerializedName("senderId") val sender: UserDTO,
    val receiverId: String, // receiverId không populate -> trả về String ID
    val status: String,
    val createdAt: String? = null
)

data class FriendRequestSentResponse(
    val success: Boolean,
    val message: String,
    val data: List<FriendRequestSentItem>
)

data class FriendRequestSentItem(
    @SerializedName("_id") val id: String,
    val senderId: String,
    @SerializedName("receiverId") val receiver: UserDTO,
    val status: String,
    val createdAt: String? = null
)

data class SendFriendRequestRequest(
    val receiverId: String
)
data class SendFriendRequestResponse(
    val success: Boolean,
    val message: String
)
data class FriendRequestActionRequest(
    val requestId: String
)