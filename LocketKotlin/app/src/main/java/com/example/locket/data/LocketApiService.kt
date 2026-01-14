package com.example.locket.data

import com.example.locket.model.BaseResponse
import com.example.locket.model.ConversationResponse
import com.example.locket.model.FriendListResponse
import com.example.locket.model.FriendRequestActionRequest
import com.example.locket.model.FriendRequestReceivedResponse
import com.example.locket.model.FriendRequestSentResponse
import com.example.locket.model.LoginRequest
import com.example.locket.model.LoginResponse
import com.example.locket.model.MessageDetailResponse
import com.example.locket.model.PictureDetailResponse
import com.example.locket.model.PictureListResponse
import com.example.locket.model.RegisterRequest
import com.example.locket.model.RegisterResponse
import com.example.locket.model.SendFriendRequestRequest
import com.example.locket.model.SendFriendRequestResponse
import com.example.locket.model.SendMessageRequest
import com.example.locket.model.SendMessageResponse
import com.example.locket.model.UserSearchResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import okhttp3.RequestBody
interface LocketApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Multipart
    @POST("picture/upload")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("message") message: RequestBody? = null,
        @Part("time") time: RequestBody? = null,
        @Part("location") location: RequestBody? = null
    ): Response<Unit>

    @GET("picture/list")
    suspend fun getPictureList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PictureListResponse>

    @GET("picture/detail/{id}")
    suspend fun getPictureDetail(@Path("id") id: String): Response<PictureDetailResponse>

    @GET("user/search")
    suspend fun searchUsers(@Query("search") query: String): Response<UserSearchResponse>

    @POST("user/friend-request")
    suspend fun sendFriendRequest(@Body request: SendFriendRequestRequest): Response<SendFriendRequestResponse>

    @GET("user/friends")
    suspend fun getMyFriends(): Response<FriendListResponse>

    @GET("user/friend-requests-recived")
    suspend fun getReceivedFriendRequests(): Response<FriendRequestReceivedResponse>

    @GET("user/friend-requests-sent")
    suspend fun getSentFriendRequests(): Response<FriendRequestSentResponse>

    @POST("user/friend-request/accept")
    suspend fun acceptFriendRequest(@Body request: FriendRequestActionRequest): Response<BaseResponse>

    @POST("user/friend-request/reject")
    suspend fun rejectFriendRequest(@Body request: FriendRequestActionRequest): Response<BaseResponse>

    @GET("user/profile")
    suspend fun getProfile(): Response<com.example.locket.model.UserProfileResponse>

    @DELETE("user/delete")
    suspend fun deleteAccount(): Response<com.example.locket.model.BaseResponse>

    @GET("message/get-conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ConversationResponse>

    @GET("message/conversation/{id}")
    suspend fun getMessagesInConversation(
        @Path("id") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<MessageDetailResponse>

    // 3. Gửi tin nhắn
    @POST("message/send-message")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>
}