package com.example.locket.model

import com.google.gson.annotations.SerializedName


data class ConversationDTO(
    @SerializedName("_id") val id: String,
    val participants: List<ParticipantDTO>,
    val pairKey: String,
    val lastMessageAt: String
)

// 2. Model cho 1 tin nhắn chi tiết
data class MessageDTO(
    @SerializedName("_id") val id: String?,
    val sender: String,
    val content: String,
    val attachedPicture: AttachedPictureDTO?,
    val attachmentUrl: String?,

    val createdAt: String
)

// Tạo thêm class này để hứng object bên trong attachedPicture
data class AttachedPictureDTO(
    @SerializedName("_id") val id: String,
    val url: String
)

// 3. Response bọc ngoài cho danh sách hội thoại
data class ConversationResponse(
    val success: Boolean,
    val message: String,
    val data: List<ConversationDTO>
)

// 4. Response bọc ngoài cho chi tiết tin nhắn
data class MessageDetailResponse(
    val success: Boolean,
    val message: String,
    val data: MessageDataWrapper
)

data class MessageDataWrapper(
    val messages: List<MessageDTO>
)

// 5. Body để gửi tin nhắn
data class SendMessageRequest(
    val receiverId: String,
    val content: String,
    val attachedPictureId: String? = null
)

// 6. Response khi gửi tin nhắn thành công
data class SendMessageResponse(
    val success: Boolean,
    val message: String
)

data class ParticipantDTO(
    @SerializedName("_id") val id: String,
    val username: String
)