package com.nurhidayaatt.storyapp.util

import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

fun Job.wrapEspressoIdlingResource(): Job {
    EspressoIdlingResource.increment()
    this.invokeOnCompletion { EspressoIdlingResource.decrement() }
    return this
}

fun <T> Flow<T>.wrapEspressoIdlingResource(): Flow<T> {
    return this.onStart { EspressoIdlingResource.increment() }
        .onCompletion { EspressoIdlingResource.decrement() }
}
