package com.devpush.features.common.utils

/**
 * Platform-agnostic string utilities for locale-aware operations
 * This is an expect object that will have actual implementations for each platform
 */
expect object StringUtils {
    
    /**
     * Converts string to lowercase using platform-appropriate locale
     * @return Lowercase string
     */
    fun String.toLowerCaseCompat(): String
    
    /**
     * Converts string to uppercase using platform-appropriate locale
     * @return Uppercase string
     */
    fun String.toUpperCaseCompat(): String
    
    /**
     * Compares two strings ignoring case using platform-appropriate locale
     * @param other The string to compare with
     * @return Comparison result (-1, 0, 1)
     */
    fun String.compareIgnoreCase(other: String): Int
    
    /**
     * Checks if two strings are equal ignoring case using platform-appropriate locale
     * @param other The string to compare with
     * @return True if strings are equal (case-insensitive)
     */
    fun String.equalsIgnoreCase(other: String): Boolean
}

/**
 * Search and filtering utilities using platform-agnostic string operations
 */
object SearchUtils {
    
    /**
     * Checks if this string contains the query string, ignoring case
     * @param query The search query
     * @return True if the string contains the query (case-insensitive)
     */
    fun String.containsIgnoreCase(query: String): Boolean {
        return with(StringUtils) {
            this@containsIgnoreCase.toLowerCaseCompat().contains(query.toLowerCaseCompat())
        }
    }
    
    /**
     * Finds all ranges where the query matches in the string (case-insensitive)
     * @param query The search query
     * @return List of text ranges where matches occur
     */
    fun String.highlightMatches(query: String): List<TextRange> {
        if (query.isEmpty()) return emptyList()
        
        val ranges = mutableListOf<TextRange>()
        val lowerText = with(StringUtils) { this@highlightMatches.toLowerCaseCompat() }
        val lowerQuery = with(StringUtils) { query.toLowerCaseCompat() }
        
        var startIndex = 0
        while (startIndex < lowerText.length) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index == -1) break
            
            ranges.add(TextRange(index, index + query.length))
            startIndex = index + query.length
        }
        
        return ranges
    }
}

/**
 * Represents a range of text for highlighting matches
 */
data class TextRange(
    val start: Int,
    val end: Int
)