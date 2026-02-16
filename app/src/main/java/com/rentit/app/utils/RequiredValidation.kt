package com.rentit.app.utils

import android.text.TextUtils
import android.widget.EditText

/**
 * RequiredValidation
 *
 * Utility class for validating form input fields.
 * Provides validation for required fields with specific rules
 */
class RequiredValidation {
    companion object {
        // validates EditText field based on field type (email, phone number, name, etc.)
        fun validateRequiredTextField(et: EditText, fieldName: String): Boolean {
            val value = et.text.toString()

            if (TextUtils.isEmpty(value)) {
                et.error = "$fieldName is required"
                return false
            }

            // field-specific validation rules
            when (fieldName.lowercase()) {
                "phone number" -> {
                    if (value.length != 10 || !value.all { it.isDigit() }) {
                        et.error = "Enter a valid 10-digit phone number"
                        return false
                    }
                }
                "email" -> {
                    // simple email validation
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
                        et.error = "Enter a valid email address"
                        return false
                    }
                }
                "name" -> {
                    if (value.length < 3 || !value.all { it.isLetter() || it.isWhitespace() }) {
                        et.error = "Enter a valid name"
                        return false
                    }
                }
            }

            return true
        }
    }
}