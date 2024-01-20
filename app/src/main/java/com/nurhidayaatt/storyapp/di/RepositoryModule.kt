package com.nurhidayaatt.storyapp.di

import android.content.Context
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.StoryRepositoryImpl
import com.nurhidayaatt.storyapp.data.source.local.room.StoryDatabase
import com.nurhidayaatt.storyapp.data.source.remote.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun providesRepository(
        apiService: ApiService,
        storyDatabase: StoryDatabase,
        @ApplicationContext context: Context,
    ): StoryRepository {
        return StoryRepositoryImpl(
            apiService = apiService,
            storyDatabase = storyDatabase,
            context = context
        )
    }
}