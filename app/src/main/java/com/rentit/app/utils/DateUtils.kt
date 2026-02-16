package com.rentit.app.utils

import java.text.SimpleDateFormat
import java.util.Date

/**
 * DateUtils
 *
 * Utility class for date formatting operations.
 * Provides methods to convert timestamps to readable date strings.
 */
class DateUtils {
    companion object {
        // converts milliseconds timestamp to formatted date string (dd/MM/yyyy)
        fun formatDate(milliseconds: Long): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            return formatter.format(Date(milliseconds))
        }
    }
}