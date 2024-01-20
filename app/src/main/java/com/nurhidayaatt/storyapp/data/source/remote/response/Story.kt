package com.nurhidayaatt.storyapp.data.source.remote.response

data class Story(
    val id: String,
    val name: String,
    val description: String,
    val photoUrl: String,
    val lat: Double? = null,
    val lon: Double? = null,
)