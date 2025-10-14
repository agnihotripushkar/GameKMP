package com.devpush.features.game.domain.model

/**
 * Comprehensive sealed class representing different types of search and filter errors
 * with specific error messages and recovery suggestions
 */
sealed class SearchFilterError : Exception() {
    abstract val userMessage: String
    abstract val technicalMessage: String
    abstract val canRetry: Boolean
    abstract val suggestedAction: String?
    
    // Network-related errors
    object NetworkError : SearchFilterError() {
        override val message: String = "Network connection failed"
        override val userMessage: String = "No internet connection"
        override val technicalMessage: String = "Network request failed - check connectivity"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "Check your internet connection and try again"
    }
    
    object TimeoutError : SearchFilterError() {
        override val message: String = "Request timed out"
        override val userMessage: String = "Request took too long"
        override val technicalMessage: String = "Network request exceeded timeout limit"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "The server is taking longer than usual. Please try again"
    }
    
    object ServerError : SearchFilterError() {
        override val message: String = "Server error occurred"
        override val userMessage: String = "Server is temporarily unavailable"
        override val technicalMessage: String = "Server returned error response (5xx)"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "Our servers are experiencing issues. Please try again in a few minutes"
    }
    
    // Database-related errors
    object DatabaseError : SearchFilterError() {
        override val message: String = "Database error occurred"
        override val userMessage: String = "Data access failed"
        override val technicalMessage: String = "Local database operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem accessing your data. Please try again"
    }
    
    object DatabaseCorruptionError : SearchFilterError() {
        override val message: String = "Database corruption detected"
        override val userMessage: String = "Data corruption detected"
        override val technicalMessage: String = "Local database integrity check failed"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please restart the app to rebuild the database"
    }
    
    // Validation errors
    data class ValidationError(val field: String, val reason: String = "") : SearchFilterError() {
        override val message: String = "Invalid $field${if (reason.isNotEmpty()) ": $reason" else ""}"
        override val userMessage: String = "Invalid input for $field"
        override val technicalMessage: String = "Validation failed for field: $field - $reason"
        override val canRetry: Boolean = false
        override val suggestedAction: String = when (field.lowercase()) {
            "search query" -> "Please enter a valid search term (at least 2 characters)"
            "rating" -> "Please select a rating between 0 and 5"
            "platform" -> "Please select at least one valid platform"
            "genre" -> "Please select at least one valid genre"
            else -> "Please check your input and try again"
        }
    }
    
    data class FilterCombinationError(val details: String) : SearchFilterError() {
        override val message: String = "Invalid filter combination: $details"
        override val userMessage: String = "Filter combination not supported"
        override val technicalMessage: String = "Filter validation failed: $details"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "This combination of filters is not supported. Please adjust your selection"
    }
    
    // Search and filter specific errors
    object NoResultsFound : SearchFilterError() {
        override val message: String = "No games found matching your criteria"
        override val userMessage: String = "No results found"
        override val technicalMessage: String = "Search/filter operation returned empty result set"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Try adjusting your search terms or filters to find more games"
    }
    
    object SearchQueryTooShort : SearchFilterError() {
        override val message: String = "Search query too short"
        override val userMessage: String = "Search term too short"
        override val technicalMessage: String = "Search query must be at least 2 characters"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please enter at least 2 characters to search"
    }
    
    object TooManyFiltersError : SearchFilterError() {
        override val message: String = "Too many filters applied"
        override val userMessage: String = "Too many filters selected"
        override val technicalMessage: String = "Maximum filter limit exceeded"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please reduce the number of selected filters"
    }
    
    // Loading and processing errors
    object FilterLoadError : SearchFilterError() {
        override val message: String = "Failed to load filter options"
        override val userMessage: String = "Filter options unavailable"
        override val technicalMessage: String = "Filter metadata loading failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "Unable to load filter options. Please refresh the page"
    }
    
    object SearchProcessingError : SearchFilterError() {
        override val message: String = "Search processing failed"
        override val userMessage: String = "Search failed to complete"
        override val technicalMessage: String = "Search algorithm encountered an error"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem processing your search. Please try again"
    }
    
    // Offline and cache errors
    object OfflineError : SearchFilterError() {
        override val message: String = "Offline mode - limited functionality"
        override val userMessage: String = "You're offline"
        override val technicalMessage: String = "Device is offline, using cached data"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "You're viewing cached data. Connect to the internet for the latest games"
    }
    
    object CacheError : SearchFilterError() {
        override val message: String = "Cache operation failed"
        override val userMessage: String = "Data caching failed"
        override val technicalMessage: String = "Local cache read/write operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem with cached data. Please refresh"
    }
    
    object StaleDataError : SearchFilterError() {
        override val message: String = "Data may be outdated"
        override val userMessage: String = "Data might be outdated"
        override val technicalMessage: String = "Cached data is older than acceptable threshold"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "You're viewing older data. Please refresh for the latest games"
    }
    
    // Performance and resource errors
    object PerformanceError : SearchFilterError() {
        override val message: String = "Operation taking too long"
        override val userMessage: String = "Processing is slow"
        override val technicalMessage: String = "Operation exceeded performance threshold"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "This is taking longer than usual. Please wait or try with fewer filters"
    }
    
    object MemoryError : SearchFilterError() {
        override val message: String = "Insufficient memory"
        override val userMessage: String = "Not enough memory"
        override val technicalMessage: String = "Operation failed due to memory constraints"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please close other apps and try again"
    }
    
    // Generic errors
    data class UnknownError(
        override val message: String,
        val originalException: Throwable? = null
    ) : SearchFilterError() {
        override val userMessage: String = "Something went wrong"
        override val technicalMessage: String = "Unexpected error: $message"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "An unexpected error occurred. Please try again"
    }
    
    // Helper methods for error categorization
    fun isNetworkRelated(): Boolean = when (this) {
        is NetworkError, is TimeoutError, is ServerError -> true
        else -> false
    }
    
    fun isUserInputError(): Boolean = when (this) {
        is ValidationError, is FilterCombinationError, is SearchQueryTooShort, is TooManyFiltersError -> true
        else -> false
    }
    
    fun isRecoverable(): Boolean = canRetry
    
    fun requiresUserAction(): Boolean = when (this) {
        is ValidationError, is FilterCombinationError, is SearchQueryTooShort, is TooManyFiltersError -> true
        else -> false
    }
}