package com.devpush.features.statistics.domain.model

/**
 * Sealed class representing different types of errors that can occur during user rating and review operations
 */
sealed class UserRatingReviewError : Exception() {
    abstract val userMessage: String
    abstract val technicalMessage: String
    abstract val canRetry: Boolean
    abstract val suggestedAction: String?
    
    // Database-related errors
    object DatabaseError : UserRatingReviewError() {
        override val message: String = "Database error occurred"
        override val userMessage: String = "Failed to save your data"
        override val technicalMessage: String = "Database operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem saving your rating/review. Please try again"
    }
    
    object GameNotFound : UserRatingReviewError() {
        override val message: String = "Game not found"
        override val userMessage: String = "Game not found"
        override val technicalMessage: String = "Referenced game does not exist in database"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "The game you're trying to rate doesn't exist. Please refresh and try again"
    }
    
    // Input validation errors
    data class InvalidGameId(val gameId: Int) : UserRatingReviewError() {
        override val message: String = "Invalid game ID: $gameId"
        override val userMessage: String = "Invalid game"
        override val technicalMessage: String = "Game ID must be positive, got: $gameId"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please select a valid game to rate"
    }
    
    // Rating validation errors
    data class InvalidRating(val rating: Int) : UserRatingReviewError() {
        override val message: String = "Invalid rating: $rating"
        override val userMessage: String = "Invalid rating"
        override val technicalMessage: String = "Rating must be between 1 and 5 stars, got: $rating"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please select a rating between 1 and 5 stars"
    }
    
    // Review validation errors
    object EmptyReview : UserRatingReviewError() {
        override val message: String = "Review text is empty"
        override val userMessage: String = "Review cannot be empty"
        override val technicalMessage: String = "Review text is blank or empty"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please write something in your review before saving"
    }
    
    object ReviewTooShort : UserRatingReviewError() {
        override val message: String = "Review text is too short"
        override val userMessage: String = "Review is too short"
        override val technicalMessage: String = "Review text is too short"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please write a longer review"
    }
    
    data class ReviewTooLong(val length: Int, val maxLength: Int = UserReview.MAX_REVIEW_LENGTH) : UserRatingReviewError() {
        override val message: String = "Review too long: $length characters (max: $maxLength)"
        override val userMessage: String = "Review is too long"
        override val technicalMessage: String = "Review text exceeds maximum length of $maxLength characters, got: $length"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please shorten your review to $maxLength characters or less"
    }
    
    data class InvalidReviewContent(val reason: String) : UserRatingReviewError() {
        override val message: String = "Invalid review content: $reason"
        override val userMessage: String = "Invalid review content"
        override val technicalMessage: String = "Review content validation failed: $reason"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please check your review content and try again"
    }
    
    data class ValidationError(val reason: String) : UserRatingReviewError() {
        override val message: String = "Validation error: $reason"
        override val userMessage: String = "Invalid input"
        override val technicalMessage: String = "Input validation failed: $reason"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please check your input and try again"
    }
    
    // Data integrity errors
    object RatingNotFound : UserRatingReviewError() {
        override val message: String = "Rating not found"
        override val userMessage: String = "No rating found"
        override val technicalMessage: String = "User rating does not exist for this game"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "You haven't rated this game yet"
    }
    
    object ReviewNotFound : UserRatingReviewError() {
        override val message: String = "Review not found"
        override val userMessage: String = "No review found"
        override val technicalMessage: String = "User review does not exist for this game"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "You haven't written a review for this game yet"
    }
    
    // Operation errors
    object RatingAlreadyExists : UserRatingReviewError() {
        override val message: String = "Rating already exists"
        override val userMessage: String = "You've already rated this game"
        override val technicalMessage: String = "User rating already exists for this game"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "You can update your existing rating instead"
    }
    
    object ReviewAlreadyExists : UserRatingReviewError() {
        override val message: String = "Review already exists"
        override val userMessage: String = "You've already reviewed this game"
        override val technicalMessage: String = "User review already exists for this game"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "You can edit your existing review instead"
    }
    
    // Network and sync errors
    object NetworkError : UserRatingReviewError() {
        override val message: String = "Network error occurred"
        override val userMessage: String = "Connection failed"
        override val technicalMessage: String = "Network operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "Check your internet connection and try again"
    }
    
    // Generic errors
    data class UnknownError(
        override val message: String,
        val originalException: Throwable? = null
    ) : UserRatingReviewError() {
        override val userMessage: String = "Something went wrong"
        override val technicalMessage: String = "Unexpected error: $message"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "An unexpected error occurred. Please try again"
    }
    
    // Helper methods for error categorization
    fun isValidationError(): Boolean = when (this) {
        is InvalidGameId, is InvalidRating, is EmptyReview, is ReviewTooLong, is InvalidReviewContent -> true
        else -> false
    }
    
    fun isDataNotFoundError(): Boolean = when (this) {
        is GameNotFound, is RatingNotFound, is ReviewNotFound -> true
        else -> false
    }
    
    fun isConflictError(): Boolean = when (this) {
        is RatingAlreadyExists, is ReviewAlreadyExists -> true
        else -> false
    }
    
    fun requiresUserAction(): Boolean = when (this) {
        is InvalidGameId, is InvalidRating, is EmptyReview, is ReviewTooLong, is InvalidReviewContent -> true
        else -> false
    }
}