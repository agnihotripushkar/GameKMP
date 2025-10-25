package com.devpush.features.common.utils

/**
 * Platform-specific date formatter for handling locale-aware date formatting
 * This is an expect class that will have actual implementations for each platform
 */
expect class PlatformDateFormatter() {
    
    /**
     * Formats a timestamp using the specified pattern
     * @param timestamp The timestamp in milliseconds since epoch
     * @param pattern The date format pattern (e.g., "MMM dd, yyyy", "yyyy-MM-dd")
     * @return Formatted date string according to the platform's locale
     */
    fun formatDate(timestamp: Long, pattern: String): String
    
    /**
     * Formats a timestamp as relative time (e.g., "2 days ago", "1 hour ago")
     * Uses platform-specific relative time formatting when available
     * @param timestamp The timestamp in milliseconds since epoch
     * @return Relative time string
     */
    fun formatRelativeTime(timestamp: Long): String
}