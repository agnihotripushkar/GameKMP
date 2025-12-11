package com.devpush.features.statistics.domain.usecase

import com.devpush.features.statistics.domain.model.UserRatingStats
import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository

/**
 * Use case for retrieving user rating statistics
 * Provides aggregated data about user's rating and review activity
 */
class GetUserRatingStatsUseCase(
    private val repository: UserRatingReviewRepository
) {
    /**
     * Retrieves comprehensive statistics about user's ratings and reviews
     * @return Result containing UserRatingStats with totals, averages, and distribution
     */
    suspend operator fun invoke(): Result<UserRatingStats> {
        return try {
            // Get statistics through the repository
            repository.getUserRatingStats()
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.UnknownError(e.message ?: "Unknown error", e))
        }
    }
}