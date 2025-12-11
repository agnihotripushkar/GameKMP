package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.repository.RecentActivity

/**
 * Use case for retrieving recent user activity (ratings and reviews)
 * Provides recent activity data for display in statistics screen
 */
class GetRecentUserActivityUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves recent user activity with game information
     * @param limit Maximum number of activities to return (default: 10)
     * @return Result containing list of RecentActivity objects
     */
    suspend operator fun invoke(limit: Int = 10): Result<List<RecentActivity>> {
        return try {
            require(limit > 0) { "Limit must be positive, got: $limit" }
            repository.getRecentUserActivity(limit)
        } catch (e: IllegalArgumentException) {
            Result.failure(UserRatingReviewError.ValidationError(e.message ?: "Invalid limit"))
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}