package com.nurhidayaatt.storyapp.data.source

import android.location.Location
import androidx.paging.PagingData
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity
import com.nurhidayaatt.storyapp.data.source.remote.response.DefaultResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.DetailStoryResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.LoginResult
import com.nurhidayaatt.storyapp.data.source.remote.response.StoriesResponse
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

interface StoryRepository {
    suspend fun createUser(
        name: String,
        email: String,
        password: String,
    ): Flow<Resource<DefaultResponse>>

    suspend fun login(email: String, password: String): Flow<Resource<LoginResult>>

    fun getAllStories(): Flow<PagingData<StoryEntity>>

    suspend fun getAllStoriesForMap(): Flow<Resource<StoriesResponse>>

    suspend fun getDetailStories(storyId: String): Flow<Resource<DetailStoryResponse>>

    suspend fun addStory(
        file: MultipartBody.Part?,
        description: RequestBody,
        location: Location?
    ): Flow<Resource<DefaultResponse>>

    fun getLocation(): Flow<LocationResponse<Location>>
}