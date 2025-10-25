package com.devpush.features.userRatingsReviews.domain.validation

import com.devpush.features.common.utils.StringUtils

/**
 * Utility object for sanitizing user input in ratings and reviews
 */
object InputSanitizer {
    
    /**
     * Sanitizes review text to prevent security issues and ensure clean data
     * @param input The raw review text input
     * @return Sanitized review text
     */
    fun sanitizeReviewText(input: String): String {
        return input
            .trim() // Remove leading/trailing whitespace
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .replace(Regex("<[^>]*>"), "") // Remove HTML tags
            .replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]"), "") // Remove control characters
            .take(1000) // Enforce maximum length
    }
    
    /**
     * Normalizes rating input to ensure it's within valid bounds
     * @param rating The raw rating input
     * @return Normalized rating (1-5) or null if invalid
     */
    fun normalizeRating(rating: Int): Int? {
        return when {
            rating in 1..5 -> rating
            else -> null
        }
    }
    
    /**
     * Checks if text contains potentially harmful content
     * @param text The text to check
     * @return true if text appears safe, false otherwise
     */
    fun isSafeText(text: String): Boolean {
        // Check for script injection attempts
        val dangerousPatterns = listOf(
            Regex("javascript:", RegexOption.IGNORE_CASE),
            Regex("data:", RegexOption.IGNORE_CASE),
            Regex("vbscript:", RegexOption.IGNORE_CASE),
            Regex("<script", RegexOption.IGNORE_CASE),
            Regex("</script>", RegexOption.IGNORE_CASE),
            Regex("onclick", RegexOption.IGNORE_CASE),
            Regex("onerror", RegexOption.IGNORE_CASE),
            Regex("onload", RegexOption.IGNORE_CASE)
        )
        
        return dangerousPatterns.none { it.containsMatchIn(text) }
    }
    
    /**
     * Removes excessive punctuation and normalizes text
     * @param text The text to normalize
     * @return Normalized text
     */
    fun normalizeText(text: String): String {
        return text
            .replace(Regex("[!]{2,}"), "!") // Replace multiple exclamation marks
            .replace(Regex("[?]{2,}"), "?") // Replace multiple question marks
            .replace(Regex("[.]{3,}"), "...") // Replace multiple dots with ellipsis
            .replace(Regex("[-]{2,}"), "--") // Replace multiple dashes
    }
    
    /**
     * Validates that text doesn't contain only repeated characters
     * @param text The text to check
     * @return true if text has variety, false if it's just repeated characters
     */
    fun hasTextVariety(text: String): Boolean {
        if (text.length < 3) return true
        
        val cleanText = with(StringUtils) { text.replace(Regex("\\s+"), "").toLowerCaseCompat() }
        if (cleanText.isEmpty()) return false
        
        // Check if more than 70% of characters are the same
        val mostCommonChar = cleanText.groupingBy { it }.eachCount().maxByOrNull { it.value }
        val mostCommonCount = mostCommonChar?.value ?: 0
        val threshold = (cleanText.length * 0.7).toInt()
        
        return mostCommonCount < threshold
    }
}