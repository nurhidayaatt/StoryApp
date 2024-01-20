package com.nurhidayaatt.storyapp.presentation.detail_story

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.remote.response.DetailStoryResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailStoryViewModel @Inject constructor(
    private val repository: StoryRepository,
) : ViewModel() {

    private val loadingDetailChannel = Channel<Boolean>()
    val loadingDetailState = loadingDetailChannel.receiveAsFlow()

    private val errorDetailChannel = Channel<String?>()
    val errorDetailState = errorDetailChannel.receiveAsFlow()

    private val _detailStoriesState = MutableStateFlow(DetailStoryResponse())
    val detailStoriesState: StateFlow<DetailStoryResponse> = _detailStoriesState

    fun getDetailStory(storyId: String) = viewModelScope.launch {
        repository.getDetailStories(storyId = storyId).collect { response ->
            when (response) {
                is Resource.Error -> {
                    loadingDetailChannel.send(element = false)
                    errorDetailChannel.send(element = response.message)
                }

                is Resource.Loading -> {
                    loadingDetailChannel.send(element = true)
                }

                is Resource.Success -> {
                    loadingDetailChannel.send(element = false)
                    _detailStoriesState.update { response.data!! }
                }
            }
        }
    }
}