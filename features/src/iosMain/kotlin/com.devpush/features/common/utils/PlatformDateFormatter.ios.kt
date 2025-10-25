package com.devpush.features.common.utils

import kotlinx.datetime.*
import platform.Foundation.*

/**
 * iOS implementation of PlatformDateFormatter using Foundation's NSDateFormatter
 */
actual class PlatformDateFormatter {
    
    actual fun formatDate(timestamp: Long, pattern: String): String {
        return try {
            val date = NSDate.dateWithTimeIntervalSince1970(timestamp / 1000.0)
            val formatter = NSDateFormatter()
            
            // Convert Java-style pattern to iOS pattern
            val iosPattern = convertPatternToIOS(pattern)
            formatter.dateFormat = iosPattern
            formatter.locale = NSLocale.currentLocale
            
            formatter.stringFromDate(date)
        } catch (e: Exception) {
            // Fallback using kotlinx-datetime
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            "${getMonthAbbreviation(localDateTime.monthNumber)} ${localDateTime.dayOfMonth.toString().padStart(2, '0')}, ${localDateTime.year}"
        }
    }
    
    actual fun formatRelativeTime(timestamp: Long): String {
        val now = Clock.System.now()
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val duration = now - instant
        
        return when {
            duration.inWholeDays > 0 -> {
                val days = duration.inWholeDays
                if (days == 1L) "1 day ago" else "$days days ago"
            }
            duration.inWholeHours > 0 -> {
                val hours = duration.inWholeHours
                if (hours == 1L) "1 hour ago" else "$hours hours ago"
            }
            duration.inWholeMinutes > 0 -> {
                val minutes = duration.inWholeMinutes
                if (minutes == 1L) "1 minute ago" else "$minutes minutes ago"
            }
            else -> "Just now"
        }
    }
    
    private fun convertPatternToIOS(javaPattern: String): String {
        // Convert common Java date patterns to iOS patterns
        return javaPattern
            .replace("yyyy", "yyyy")
            .replace("MM", "MM")
            .replace("dd", "dd")
            .replace("MMM", "MMM")
    }
    
    private fun getMonthAbbreviation(month: Int): String {
        return when (month) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
            else -> "Jan"
        }
    }
}