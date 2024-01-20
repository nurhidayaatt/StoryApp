package com.nurhidayaatt.storyapp.util

import android.view.View

inline fun <T: View> T.showIfOrInvisible(condition: (T) -> Boolean) {
    if (condition(this)) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.INVISIBLE
    }
}