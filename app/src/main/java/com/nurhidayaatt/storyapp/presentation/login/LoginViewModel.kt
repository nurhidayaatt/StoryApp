package com.nurhidayaatt.storyapp.presentation.login

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nurhidayaatt.storyapp.data.source.Resource
import com.nurhidayaatt.storyapp.data.source.StoryRepository
import com.nurhidayaatt.storyapp.data.source.local.preferences.UserSessionKeys
import com.nurhidayaatt.storyapp.data.source.remote.response.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: StoryRepository,
    private val userSession: DataStore<Preferences>
) : ViewModel() {

    val userToken = userSession.data.map { pref -> pref[UserSessionKeys.TOKEN] ?: "" }

    private val loginChannel = Channel<Resource<LoginResult>>()
    val loginState = loginChannel.receiveAsFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        repository.login(email, password).collect { data ->
            loginChannel.send(data)
        }
    }

    fun saveSession(token: String) = viewModelScope.launch {
        userSession.edit { preferences -> preferences[UserSessionKeys.TOKEN] = token }
    }
}