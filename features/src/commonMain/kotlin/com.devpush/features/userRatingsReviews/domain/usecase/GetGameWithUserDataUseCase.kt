package com.devpush.features.userRatingsReviews.domain.usecase

import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.userRatingsReviews.domain.model.UserRatingReviewError
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository

/**
 * Use case for retrieving a single game with its associated user data
 * Combines game information with user's personal rating and review
 */
class GetGameWithUserDataUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves a game with its associated user rating and review data
     * @param gameId The ID of the game to retrieve
     * @return Result containing GameWithUserData or null if game not found
     */
    suspend operator fun invoke(gameId: Int): Result<GameWithUserData?> {
        return try {
            // Validate game ID
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.InvalidGameId(gameId))
            }
            
            // Get the game with user data through the repository
            repository.getGameWithUserData(gameId)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}