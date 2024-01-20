package com.nurhidayaatt.storyapp.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.remote.response.DefaultResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: StoryRepository
): ViewModel() {

    private val registerChanel = Channel<Resource<DefaultResponse>>()
    val registerState = registerChanel.receiveAsFlow()

    fun createUSer(name: String, email: String, password: String) = viewModelScope.launch {
        repository.createUser(name = name, email = email, password = password).collect {
            registerChanel.send(it)
        }
    }
}