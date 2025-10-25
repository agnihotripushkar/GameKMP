package com.devpush.features.common.utils

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

/**
 * Android implementation of PlatformDateFormatter using Android's date formatting APIs
 */
actual class PlatformDateFormatter {
    
    actual fun formatDate(timestamp: Long, pattern: String): String {
        return try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            formatter.format(Date(timestamp))
        } catch (e: Exception) {
            // Fallback to ISO format if pattern is invalid
            val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            formatter.format(Date(timestamp))
        }
    }
    
    actual fun formatRelativeTime(timestamp: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}