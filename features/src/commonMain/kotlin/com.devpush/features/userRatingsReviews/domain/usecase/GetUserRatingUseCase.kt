package com.devpush.features.userRatingsReviews.domain.usecase

import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserRatingReviewError
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository

/**
 * Use case for retrieving a user's rating for a specific game
 * Handles validation and business logic for rating retrieval
 */
class GetUserRatingUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves a user's rating for a specific game
     * @param gameId The ID of the game
     * @return Result containing the UserRating or null if not found, or failure with error
     */
    suspend operator fun invoke(gameId: Int): Result<UserRating?> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Get the rating through the repository
            repository.getUserRating(gameId)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown database error", e))
        }
    }
}