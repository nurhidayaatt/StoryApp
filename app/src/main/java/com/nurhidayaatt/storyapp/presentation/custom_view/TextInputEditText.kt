package com.nurhidayaatt.storyapp.presentation.custom_view

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Patterns.EMAIL_ADDRESS
import androidx.core.widget.addTextChangedListener
import com.google.android.material.textfield.TextInputEditText
import com.nurhidayaatt.storyapp.R

class TextInputEditText: TextInputEditText {

    private var inputType: Int = getInputType()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        addTextChangedListener { editable ->
            editable?.let {
                when (inputType-1) {
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                        error = if (it.isNotBlank() && !EMAIL_ADDRESS.matcher(it).matches()) {
                            context.getString(R.string.error_email)
                        } else {
                            null
                        }
                    }
                    InputType.TYPE_TEXT_VARIATION_PASSWORD -> {
                        error = if (it.isNotBlank() && it.length < 8) {
                            context.getString(R.string.error_password)
                        } else {
                            null
                        }
                    }
                }
            }
        }
    }
}