package com.nurhidayaatt.storyapp.presentation.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.local.entity.StoryEntity
import com.nurhidayaatt.storyapp.data.source.local.preferences.UserSessionKeys
import com.nurhidayaatt.storyapp.util.wrapEspressoIdlingResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userSession: DataStore<Preferences>,
    private val repository: StoryRepository
) : ViewModel() {

    fun deleteSession() = viewModelScope.launch {
        userSession.edit { preferences -> preferences[UserSessionKeys.TOKEN] = "" }
    }.wrapEspressoIdlingResource()

    init {
        getAllStories()
    }

    var stories: Flow<PagingData<StoryEntity>>? = null

    var refreshInProgress = false
    var pendingScrollToTopAfterRefresh = false
    var newDataInProgress = true

    private fun getAllStories() {
        newDataInProgress = true
        stories = repository.getAllStories().cachedIn(viewModelScope)
    }
}