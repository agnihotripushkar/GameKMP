package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.GameWithUserData
import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository

/**
 * Use case for retrieving multiple games with their associated user data
 * Optimized for batch operations and collection views
 */
class GetGamesWithUserDataUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves multiple games with their associated user rating and review data
     * @param gameIds List of game IDs to retrieve (empty list returns empty result)
     * @return Result containing list of GameWithUserData objects
     */
    suspend operator fun invoke(gameIds: List<Int>): Result<List<GameWithUserData>> {
        return try {
            // Handle empty input
            if (gameIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Validate all game IDs
            val invalidGameIds = gameIds.filter { it <= 0 }
            if (invalidGameIds.isNotEmpty()) {
                return Result.failure(
                    UserRatingReviewError.InvalidGameId(invalidGameIds.first())
                )
            }
            
            // Optimize for large datasets by chunking requests if needed
            val result = if (gameIds.size > MAX_BATCH_SIZE) {
                // Process in chunks to avoid overwhelming the database
                val chunkedResults = gameIds.chunked(MAX_BATCH_SIZE).map { chunk ->
                    repository.getGamesWithUserData(chunk)
                }
                
                // Combine all results
                val allGames = mutableListOf<GameWithUserData>()
                for (chunkResult in chunkedResults) {
                    if (chunkResult.isFailure) {
                        return chunkResult // Return first failure
                    }
                    allGames.addAll(chunkResult.getOrThrow())
                }
                
                Result.success(allGames)
            } else {
                // Process all at once for smaller datasets
                repository.getGamesWithUserData(gameIds)
            }
            
            result
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
    
    companion object {
        /**
         * Maximum number of games to process in a single batch to optimize performance
         */
        private const val MAX_BATCH_SIZE = 100
    }
}