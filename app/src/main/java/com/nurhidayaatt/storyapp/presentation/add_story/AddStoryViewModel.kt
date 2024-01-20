package com.nurhidayaatt.storyapp.presentation.add_story

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.common.api.ResolvableApiException
import com.nurhidayaatt.storyapp.data.source.LocationResponse
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.remote.response.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

@HiltViewModel
class AddStoryViewModel @Inject constructor(
    private val repository: StoryRepository
): ViewModel() {

    private val addStoriesChannel = Channel<Resource<DefaultResponse>>()
    val addStoriesState = addStoriesChannel.receiveAsFlow()

    private val exceptionChannel = Channel<ResolvableApiException>()
    val exceptionState = exceptionChannel.receiveAsFlow()

    private val _fileImage = MutableStateFlow<File?>(value = null)
    val fileImage: StateFlow<File?> = _fileImage

    private var imageMultipart: MultipartBody.Part? = null

    private var _shouldAddLocation = MutableStateFlow(false)
    var shouldAddLocation: StateFlow<Boolean> = _shouldAddLocation

    fun changeLocationInformation(shouldAddLocation: Boolean) = _shouldAddLocation.update { shouldAddLocation }

    fun setImage(file: File) {
        _fileImage.update { file }
        val requestImageFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        imageMultipart = MultipartBody.Part.createFormData("photo", file.name, requestImageFile)
    }

    fun addStory(imageDescription: String) = viewModelScope.launch {
        addStoriesChannel.send(Resource.Loading())
        val description = imageDescription.toRequestBody("text/plain".toMediaType())
        if (shouldAddLocation.value) {
            repository.getLocation().take(count = 1).collect {
                when (it) {
                    is LocationResponse.Success -> {
                        uploadStory(description = description, location = it.data)
                    }
                    is LocationResponse.Error -> {
                        addStoriesChannel.send(Resource.Error(""))
                        exceptionChannel.send(it.exception!!)
                    }
                }
            }
        } else {
            uploadStory(description = description, location = null)
        }
    }

    private suspend fun uploadStory(description: RequestBody, location: Location? = null) {
        repository.addStory(
            file = imageMultipart,
            description = description,
            location = location
        ).collect { result ->
            addStoriesChannel.send(result)
        }
    }
}