package com.rentit.app.utils

import android.text.TextUtils
import android.widget.EditText

class RequiredValidation {
    companion object {
        fun validateRequiredTextField(et: EditText, fieldName: String): Boolean {
            val value = et.text.toString()

            if (TextUtils.isEmpty(value)) {
                et.error = "$fieldName is required"
                return false
            }

            return true
        }
    }
}