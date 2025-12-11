package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.validation.UserRatingReviewValidator
import com.devpush.features.statistics.domain.validation.ValidationResult

/**
 * Use case for setting or updating a user's rating for a game
 * Handles validation and business logic for rating operations
 */
class SetUserRatingUseCase(
    private val repository: UserRatingReviewRepository,
    private val validator: UserRatingReviewValidator
) {
    /**
     * Sets or updates a user's rating for a game
     * @param gameId The ID of the game to rate
     * @param rating The rating value (1-5 stars)
     * @return Result indicating success or failure with specific error information
     */
    suspend operator fun invoke(gameId: Int, rating: Int): Result<Unit> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Validate rating using the validator
            val validationResult = validator.validateRating(rating)
            if (!validationResult.isValid) {
                return Result.failure((validationResult as ValidationResult.Error).error)
            }
            
            // Set the rating through the repository
            repository.setUserRating(gameId, rating)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown database error", e))
        }
    }
}