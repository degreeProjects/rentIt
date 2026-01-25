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

            // 2. Conditional Checks based on fieldName
            when (fieldName.lowercase()) {
                "phone number" -> {
                    if (value.length != 10 || !value.all { it.isDigit() }) {
                        et.error = "Enter a valid 10-digit phone number"
                        return false
                    }
                }
                "email" -> {
                    // Android's built-in email pattern matcher
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                        et.error = "Enter a valid email address"
                        return false
                    }
                }
                "name" -> {
                    if(value.length < 3 || !value.all { it.isLetter() }) {
                        et.error = "Enter a valid name"
                        return false
                    }
                }
            }

            return true
        }
    }
}