package com.devpush.features.userRatingsReviews.domain.usecase

import com.devpush.features.userRatingsReviews.domain.model.UserRatingReviewError
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository
import com.devpush.features.userRatingsReviews.domain.validation.UserRatingReviewValidator
import com.devpush.features.userRatingsReviews.domain.validation.ValidationResult
import com.devpush.features.userRatingsReviews.domain.validation.InputSanitizer

/**
 * Use case for setting or updating a user's review for a game
 * Handles validation, sanitization, and business logic for review operations
 */
class SetUserReviewUseCase(
    private val repository: UserRatingReviewRepository,
    private val validator: UserRatingReviewValidator,
    private val inputSanitizer: InputSanitizer
) {
    /**
     * Sets or updates a user's review for a game
     * @param gameId The ID of the game to review
     * @param reviewText The review text (will be sanitized and validated)
     * @return Result indicating success or failure with specific error information
     */
    suspend operator fun invoke(gameId: Int, reviewText: String): Result<Unit> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Sanitize the review text
            val sanitizedReviewText = inputSanitizer.sanitizeReviewText(reviewText)
            
            // Validate review text using the validator
            val validationResult = validator.validateReviewText(sanitizedReviewText)
            if (!validationResult.isValid) {
                return Result.failure((validationResult as ValidationResult.Error).error)
            }
            
            // Set the review through the repository
            repository.setUserReview(gameId, sanitizedReviewText)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}