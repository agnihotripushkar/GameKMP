package com.devpush.features.userRatingsReviews.domain.validation

import com.devpush.features.userRatingsReviews.domain.model.UserRatingReviewError
import com.devpush.features.userRatingsReviews.domain.model.UserReview

/**
 * Validator for user ratings and reviews to ensure valid input and prevent invalid data
 */
object UserRatingReviewValidator {
    
    // Configuration constants
    private const val MIN_RATING = 1
    private const val MAX_RATING = 5
    private const val MAX_REVIEW_LENGTH = UserReview.MAX_REVIEW_LENGTH
    private const val MIN_REVIEW_LENGTH = 1
    
    /**
     * Validates a user rating value
     * @param rating The rating to validate (1-5 stars)
     * @return ValidationResult indicating success or specific error
     */
    fun validateRating(rating: Int): ValidationResult {
        return when {
            rating < MIN_RATING || rating > MAX_RATING -> ValidationResult.Error(
                UserRatingReviewError.InvalidRating(rating)
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates review text content
     * @param reviewText The review text to validate
     * @return ValidationResult indicating success or specific error
     */
    fun validateReviewText(reviewText: String): ValidationResult {
        return when {
            reviewText.isBlank() -> ValidationResult.Error(
                UserRatingReviewError.EmptyReview
            )
            reviewText.length > MAX_REVIEW_LENGTH -> ValidationResult.Error(
                UserRatingReviewError.ReviewTooLong(reviewText.length, MAX_REVIEW_LENGTH)
            )
            reviewText.length < MIN_REVIEW_LENGTH -> ValidationResult.Error(
                UserRatingReviewError.EmptyReview
            )
            containsInvalidContent(reviewText) -> ValidationResult.Error(
                UserRatingReviewError.InvalidReviewContent("Contains inappropriate content")
            )
            containsOnlyWhitespace(reviewText) -> ValidationResult.Error(
                UserRatingReviewError.EmptyReview
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates game ID
     * @param gameId The game ID to validate
     * @return ValidationResult indicating success or specific error
     */
    fun validateGameId(gameId: Int): ValidationResult {
        return when {
            gameId <= 0 -> ValidationResult.Error(
                UserRatingReviewError.GameNotFound
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates timestamp values
     * @param timestamp The timestamp to validate
     * @return ValidationResult indicating success or specific error
     */
    fun validateTimestamp(timestamp: Long): ValidationResult {
        return when {
            timestamp <= 0 -> ValidationResult.Error(
                UserRatingReviewError.UnknownError("Invalid timestamp: $timestamp")
            )
            timestamp > System.currentTimeMillis() + 86400000 -> ValidationResult.Error(
                UserRatingReviewError.UnknownError("Timestamp cannot be in the future")
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates that updated timestamp is not before created timestamp
     * @param createdAt The creation timestamp
     * @param updatedAt The update timestamp
     * @return ValidationResult indicating success or specific error
     */
    fun validateTimestampOrder(createdAt: Long, updatedAt: Long): ValidationResult {
        return when {
            updatedAt < createdAt -> ValidationResult.Error(
                UserRatingReviewError.UnknownError("Updated timestamp cannot be before created timestamp")
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Sanitizes review text by removing potentially harmful content
     * @param reviewText The original review text
     * @return Sanitized review text
     */
    fun sanitizeReviewText(reviewText: String): String {
        return reviewText
            .trim()
            .replace(Regex("\\s+"), " ") // Replace multiple whitespace with single space
            .take(MAX_REVIEW_LENGTH) // Ensure length limit
    }
    
    /**
     * Checks for potentially inappropriate content in review text
     * This is a basic implementation - in a real app you might use more sophisticated filtering
     */
    private fun containsInvalidContent(reviewText: String): Boolean {
        // Basic check for HTML/script tags that might indicate injection attempts
        val htmlPattern = Regex("<[^>]+>", RegexOption.IGNORE_CASE)
        val scriptPattern = Regex("(javascript:|data:|vbscript:)", RegexOption.IGNORE_CASE)
        
        return htmlPattern.containsMatchIn(reviewText) || 
               scriptPattern.containsMatchIn(reviewText)
    }
    
    /**
     * Checks if review text contains only whitespace characters
     */
    private fun containsOnlyWhitespace(reviewText: String): Boolean {
        return reviewText.trim().isEmpty()
    }
    
    /**
     * Provides suggestions for fixing validation errors
     */
    fun suggestCorrection(error: UserRatingReviewError): String? {
        return when (error) {
            is UserRatingReviewError.InvalidRating -> 
                "Please select a rating between $MIN_RATING and $MAX_RATING stars"
            is UserRatingReviewError.EmptyReview -> 
                "Please write something in your review"
            is UserRatingReviewError.ReviewTooLong -> 
                "Please shorten your review to ${error.maxLength} characters or less (currently ${error.length} characters)"
            is UserRatingReviewError.InvalidReviewContent -> 
                "Please remove any HTML tags or special characters from your review"
            is UserRatingReviewError.GameNotFound -> 
                "Please select a valid game to rate or review"
            else -> null
        }
    }
    
    /**
     * Gets the remaining character count for a review
     */
    fun getRemainingCharacters(reviewText: String): Int {
        return (MAX_REVIEW_LENGTH - reviewText.length).coerceAtLeast(0)
    }
    
    /**
     * Checks if review text is approaching the character limit
     */
    fun isApproachingLimit(reviewText: String, warningThreshold: Double = 0.9): Boolean {
        return reviewText.length >= (MAX_REVIEW_LENGTH * warningThreshold)
    }
}

/**
 * Result of validation operation for user ratings and reviews
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Warning(val message: String) : ValidationResult()
    data class Error(val error: UserRatingReviewError) : ValidationResult()
    
    val isValid: Boolean get() = this is Success || this is Warning
    val hasWarning: Boolean get() = this is Warning
    val hasError: Boolean get() = this is Error
}