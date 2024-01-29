package com.nurhidayaatt.storyapp.presentation.custom_view

import android.content.Context
import android.graphics.Canvas
import android.text.InputType
import android.util.AttributeSet
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.nurhidayaatt.storyapp.R


class TextInputLayout: TextInputLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        editText?.let { editText ->
            when(editText.inputType-1) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                    endIconMode = if (editText.error == null) {
                        END_ICON_PASSWORD_TOGGLE
                    } else {
                        END_ICON_NONE
                    }
                    startIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_lock)
                }
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                    startIconDrawable = ContextCompat.getDrawable(context, R.drawable.ic_email)
                }
            }
        }
    }
}