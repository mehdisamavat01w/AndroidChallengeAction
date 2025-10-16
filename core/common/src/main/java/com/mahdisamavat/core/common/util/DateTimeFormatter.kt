package com.mahdisamavat.core.common.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object DateTimeFormatter {
    private const val PATTERN_DISPLAY = "MMM dd, yyyy hh:mm a"

    fun formatDisplay(timestamp: Long): String {
        return SimpleDateFormat(PATTERN_DISPLAY, Locale.getDefault())
            .format(Date(timestamp))
    }
}
