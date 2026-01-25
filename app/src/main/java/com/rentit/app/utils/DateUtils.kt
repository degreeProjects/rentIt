package com.rentit.app.utils

import java.text.SimpleDateFormat
import java.util.Date

class DateUtils {
    companion object {
        fun formatDate(milliseconds: Long): String {
            val formatter = SimpleDateFormat("dd/MM/yyyy")
            return formatter.format(Date(milliseconds))
        }
    }
}