package com.example.utils

import java.util.Locale

object TimeFormatter {
    fun formatSecondsToMinutes(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    fun formatMillis(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        return formatSecondsToMinutes(totalSeconds)
    }
}
