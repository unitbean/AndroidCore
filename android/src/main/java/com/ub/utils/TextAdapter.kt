package com.ub.utils

import android.text.Editable
import android.text.TextWatcher

@Deprecated(
    message = "Please use the TextView.addTextChangedListener extension of AndroidX Core-ktx"
)
abstract class TextAdapter : TextWatcher {
    override fun afterTextChanged(s: Editable?) {
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }
}