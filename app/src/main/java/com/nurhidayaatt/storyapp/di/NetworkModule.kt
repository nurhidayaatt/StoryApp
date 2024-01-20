package com.nurhidayaatt.storyapp.di

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.nurhidayaatt.storyapp.BuildConfig
import com.nurhidayaatt.storyapp.data.source.local.preferences.UserSessionKeys
import com.nurhidayaatt.storyapp.data.source.remote.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(ViewModelComponent::class)
object NetworkModule {

    @Provides
    @ViewModelScoped
    fun provideOkHttpClient(dataStore: DataStore<Preferences>): OkHttpClient {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(Interceptor {
                val userToken = "Bearer ${runBlocking { dataStore.data.first()[UserSessionKeys.TOKEN] }}"
                Log.d("provideAuthenticatedOkHttpClient", userToken)
                val request = it.request().newBuilder()
                    .header("Authorization", userToken)
                    .build()
                it.proceed(request)
            })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
        if (BuildConfig.DEBUG) {
            okHttpClient.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        }
        return okHttpClient.build()
    }

    @Provides
    @ViewModelScoped
    fun provideApiService(
        client: OkHttpClient
    ): ApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }
}