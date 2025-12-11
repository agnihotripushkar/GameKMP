package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.UserReview
import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository

/**
 * Use case for retrieving a user's review for a specific game
 * Handles validation and business logic for review retrieval
 */
class GetUserReviewUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves a user's review for a specific game
     * @param gameId The ID of the game
     * @return Result containing the UserReview or null if not found, or failure with error
     */
    suspend operator fun invoke(gameId: Int): Result<UserReview?> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Get the review through the repository
            repository.getUserReview(gameId)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}