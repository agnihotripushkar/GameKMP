package com.devpush.features.common.utils

import kotlinx.datetime.*

/**
 * Common date/time utilities using kotlinx-datetime for cross-platform compatibility
 */
object DateTimeUtils {
    
    /**
     * Formats a timestamp (milliseconds since epoch) to a readable date string
     * @param timestamp The timestamp in milliseconds
     * @return Formatted date string (e.g., "Dec 25, 2023")
     */
    fun formatTimestamp(timestamp: Long): String {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return PlatformDateFormatter().formatDate(timestamp, "MMM dd, yyyy")
    }
    
    /**
     * Gets the current timestamp in milliseconds
     * @return Current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return Clock.System.now().toEpochMilliseconds()
    }
    
    /**
     * Formats a timestamp as relative time (e.g., "2 days ago", "1 hour ago")
     * @param timestamp The timestamp in milliseconds
     * @return Relative time string
     */
    fun formatRelativeDate(timestamp: Long): String {
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
    
    /**
     * Converts a timestamp to LocalDateTime in the system timezone
     * @param timestamp The timestamp in milliseconds
     * @return LocalDateTime instance
     */
    fun timestampToLocalDateTime(timestamp: Long): LocalDateTime {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }
    
    /**
     * Converts LocalDateTime to timestamp in milliseconds
     * @param localDateTime The LocalDateTime instance
     * @return Timestamp in milliseconds
     */
    fun localDateTimeToTimestamp(localDateTime: LocalDateTime): Long {
        return localDateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
    }
}