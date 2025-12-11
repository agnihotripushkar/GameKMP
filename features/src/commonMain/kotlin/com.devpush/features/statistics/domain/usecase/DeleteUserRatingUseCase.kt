package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository

/**
 * Use case for deleting a user's rating for a specific game
 * Handles validation and business logic for rating deletion
 */
class DeleteUserRatingUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Deletes a user's rating for a specific game
     * @param gameId The ID of the game
     * @return Result indicating success or failure with specific error information
     */
    suspend operator fun invoke(gameId: Int): Result<Unit> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Delete the rating through the repository
            repository.deleteUserRating(gameId)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown database error", e))
        }
    }
}