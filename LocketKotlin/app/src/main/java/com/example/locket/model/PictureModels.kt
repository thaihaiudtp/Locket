package com.example.locket.model

import com.google.gson.annotations.SerializedName

data class PictureListResponse(
    val success: Boolean,
    val message: String,
    val meta: MetaData,
    val data: List<PictureData>
)
data class MetaData(
    val total: Int,
    val page: String, // API trả về string nên để string hoặc int tùy bạn convert
    val limit: String,
    val totalPages: Int
)
data class PictureData(
    @SerializedName("_id") val id: String,
    val url: String,
    val uploader: Uploader
)
data class Uploader(
    @SerializedName("_id") val id: String,
    val username: String
)

data class PictureDetailResponse(
    val success: Boolean,
    val message: String,
    val data: PictureDetailData
)

data class PictureDetailData(
    @SerializedName("_id") val id: String,
    val url: String,
    val uploader: UploaderDetail,
    val uploadAt: String, // Chuỗi thời gian: "2025-12-17T01:26:30.634Z"
    val reactions: List<Reaction> = emptyList()
)

data class UploaderDetail(
    @SerializedName("_id") val id: String,
    val username: String,
    val email: String
)

data class Reaction(
    val userId: String,
    val type: String // Ví dụ: "heart", "fire"
)