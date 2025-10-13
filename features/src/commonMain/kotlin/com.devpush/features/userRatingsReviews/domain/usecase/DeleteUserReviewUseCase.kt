package com.devpush.features.userRatingsReviews.domain.usecase

import com.devpush.features.userRatingsReviews.domain.model.UserRatingReviewError
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository

/**
 * Use case for deleting a user's review for a specific game
 * Handles validation and business logic for review deletion
 */
class DeleteUserReviewUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Deletes a user's review for a specific game
     * @param gameId The ID of the game
     * @return Result indicating success or failure with specific error information
     */
    suspend operator fun invoke(gameId: Int): Result<Unit> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Delete the review through the repository
            repository.deleteUserReview(gameId)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}