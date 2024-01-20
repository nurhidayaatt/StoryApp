package com.nurhidayaatt.storyapp.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.Marker
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.remote.response.Story
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MapViewModel @Inject constructor(private val repository: StoryRepository) : ViewModel() {

    private val loadingChannel = Channel<Boolean>()
    val loadingState = loadingChannel.receiveAsFlow()

    private val errorChannel = Channel<String?>()
    val errorState = errorChannel.receiveAsFlow()

    private val _storiesMapState = MutableStateFlow<List<Story>>(value = listOf())
    val storiesMapState: StateFlow<List<Story>> = _storiesMapState

    private val _selectedMarker = MutableStateFlow(MarkerState())
    val selectedMarker: StateFlow<MarkerState> = _selectedMarker

    val allMarker: MutableList<Marker?> = mutableListOf()

    init {
        getAllStory()
    }

    private fun getAllStory() = viewModelScope.launch {
        repository.getAllStoriesForMap().collect { response ->
            when (response) {
                is Resource.Error -> {
                    loadingChannel.send(element = false)
                    errorChannel.send(element = response.message)
                }

                is Resource.Loading -> {
                    loadingChannel.send(element = true)
                }

                is Resource.Success -> {
                    loadingChannel.send(element = false)
                    _storiesMapState.update { response.data!!.listStory }
                }
            }
        }
    }

    fun setSelectedMarker(marker: Marker? = null) {
        _selectedMarker.update {
            MarkerState(
                prevMarker = selectedMarker.value.selectedMarker,
                selectedMarker = marker
            )
        }
    }

    fun resetSelectedMarker() {
        _selectedMarker.update { MarkerState() }
    }
}