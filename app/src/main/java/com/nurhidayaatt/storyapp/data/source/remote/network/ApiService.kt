package com.nurhidayaatt.storyapp.data.source.remote.network

import com.nurhidayaatt.storyapp.data.source.remote.response.DefaultResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.DetailStoryResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.LoginResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.StoriesResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @FormUrlEncoded
    @POST("register")
    suspend fun createUser(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): DefaultResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun getAllStories(
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = 10,
        @Query("location") location: Int? = 0
    ): StoriesResponse

    @GET("stories/{id}")
    suspend fun getDetailStories(
        @Path("id") id: String
    ): DetailStoryResponse

    @Multipart
    @POST("stories")
    suspend fun addStory(
        @Part file: MultipartBody.Part?,
        @Part("description") description: RequestBody,
        @Part("lat") lat: Float? = null,
        @Part("lon") lon: Float? = null
    ): DefaultResponse
}