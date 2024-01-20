package com.nurhidayaatt.storyapp.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.Priority
import com.google.android.gms.location.SettingsClient
import com.google.gson.Gson
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity
import com.nurhidayaatt.storyapp.data.source.local.room.StoryDatabase
import com.nurhidayaatt.storyapp.data.source.remote.network.ApiService
import com.nurhidayaatt.storyapp.data.source.remote.response.DefaultResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.DetailStoryResponse
import com.nurhidayaatt.storyapp.data.source.remote.response.LoginResult
import com.nurhidayaatt.storyapp.data.source.remote.response.StoriesResponse
import com.nurhidayaatt.storyapp.util.wrapEspressoIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepositoryImpl(
    private val apiService: ApiService,
    private val storyDatabase: StoryDatabase,
    private val context: Context
): StoryRepository {

    override suspend fun createUser(
        name: String,
        email: String,
        password: String
    ): Flow<Resource<DefaultResponse>> = flow {
        emit(Resource.Loading())
        val response = apiService.createUser(name = name, email = email, password = password)
        emit(Resource.Success(data = response))
    }.catch {
        if (it is HttpException) {
            emit(
                Resource.Error(
                    message = Gson().fromJson(
                        it.response()?.errorBody()?.charStream(),
                        DefaultResponse::class.java
                    ).message
                )
            )
        } else {
            emit(Resource.Error(message = it.localizedMessage!!))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun login(email: String, password: String): Flow<Resource<LoginResult>> = flow {
        emit(Resource.Loading())

        val response = apiService.login(email = email, password = password)
        emit(Resource.Success(data = response.loginResult))
    }.catch {
        if (it is HttpException) {
            emit(
                Resource.Error(
                    message = Gson().fromJson(
                        it.response()?.errorBody()?.charStream(),
                        DefaultResponse::class.java
                    ).message
                )
            )
        } else {
            emit(Resource.Error(message = it.localizedMessage!!))
        }
    }.flowOn(Dispatchers.IO).wrapEspressoIdlingResource()

    @OptIn(ExperimentalPagingApi::class)
    override fun getAllStories(): Flow<PagingData<StoryEntity>> =
        Pager(
            config = PagingConfig(pageSize = 10),
            remoteMediator = StoryRemoteMediator(database = storyDatabase, apiService = apiService),
            pagingSourceFactory = {
                storyDatabase.storyDao().getAllStory()
            }
        ).flow

    override suspend fun getAllStoriesForMap(): Flow<Resource<StoriesResponse>> = flow {
        emit(Resource.Loading())
        val response = apiService.getAllStories(location = 1)
        emit(Resource.Success(data = response))
    }.catch {
        if (it is HttpException) {
            emit(
                Resource.Error(
                    message = Gson().fromJson(
                        it.response()?.errorBody()?.charStream(),
                        DefaultResponse::class.java
                    ).message
                )
            )
        } else {
            emit(Resource.Error(message = it.localizedMessage!!))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun getDetailStories(
        storyId: String
    ): Flow<Resource<DetailStoryResponse>> = flow {
        emit(Resource.Loading())
        val response = apiService.getDetailStories(id = storyId)
        emit(Resource.Success(data = response))
    }.catch {
        if (it is HttpException) {
            emit(
                Resource.Error(
                    message = Gson().fromJson(
                        it.response()?.errorBody()?.charStream(),
                        DefaultResponse::class.java
                    ).message
                )
            )
        } else {
            emit(Resource.Error(message = it.localizedMessage!!))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun addStory(
        file: MultipartBody.Part?,
        description: RequestBody,
        location: Location?
    ): Flow<Resource<DefaultResponse>> = flow {
        emit(Resource.Loading())
        val response = if (location != null) {
            apiService.addStory(
                file = file,
                description = description,
                lat = location.latitude.toFloat(),
                lon = location.longitude.toFloat()
            )
        } else {
            apiService.addStory(file = file, description = description)
        }
        emit(Resource.Success(data = response))
    }.catch {
        if (it is HttpException) {
            emit(
                Resource.Error(
                    message = Gson().fromJson(
                        it.response()?.errorBody()?.charStream(),
                        DefaultResponse::class.java
                    ).message
                )
            )
        } else {
            emit(Resource.Error(message = it.localizedMessage!!))
        }
    }.flowOn(Dispatchers.IO)

    @SuppressLint("MissingPermission")
    override fun getLocation(): Flow<LocationResponse<Location>> = callbackFlow {
        val client = LocationServices.getFusedLocationProviderClient(context)

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient: SettingsClient = LocationServices.getSettingsClient(context)

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.lastOrNull()?.let {
                    Log.d("getLocation", "$it")
                    trySend(LocationResponse.Success(it))
                }
            }
        }

        settingsClient.checkLocationSettings(builder.build()).addOnSuccessListener {
            client.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }.addOnFailureListener { exception ->
            Log.d("getLocation", "$exception")
            if (exception is ResolvableApiException) trySend(LocationResponse.Error(exception))
        }

        awaitClose {
            client.removeLocationUpdates(locationCallback)
        }
    }
}