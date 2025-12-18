package com.example.locket.data
import com.example.locket.model.LoginRequest
import com.example.locket.model.LoginResponse
import com.example.locket.model.PictureDetailResponse
import com.example.locket.model.PictureListResponse
import com.example.locket.model.RegisterRequest
import com.example.locket.model.RegisterResponse
import com.example.locket.model.UserSearchResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
interface LocketApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @Multipart
    @POST("picture/upload")
    suspend fun uploadImage(@Part file: MultipartBody.Part): Response<Unit>

    @GET("picture/list")
    suspend fun getPictureList(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<PictureListResponse>

    @GET("picture/detail/{id}")
    suspend fun getPictureDetail(@Path("id") id: String): Response<PictureDetailResponse>

    @GET("user/search")
    suspend fun searchUsers(@Query("search") query: String): Response<UserSearchResponse>
}