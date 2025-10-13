package com.devpush.kmp.userRatingsReviews

import com.devpush.features.userRatingsReviews.domain.model.*
import com.devpush.features.userRatingsReviews.domain.repository.UserRatingReviewRepository
import com.devpush.features.userRatingsReviews.domain.repository.RecentActivity
import com.devpush.features.userRatingsReviews.domain.repository.ActivityType
import kotlinx.datetime.Clock

/**
 * Fake implementation of UserRatingReviewRepository for testing purposes.
 * Simulates database operations with in-memory storage.
 */
class FakeUserRatingReviewRepository : UserRatingReviewRepository {
    
    private val ratings = mutableMapOf<Int, UserRating>()
    private val reviews = mutableMapOf<Int, UserReview>()
    
    override suspend fun setUserRating(gameId: Int, rating: Int): Result<Unit> {
        return try {
            if (rating < 1 || rating > 5) {
                Result.failure(UserRatingReviewError.InvalidRating(rating))
            } else {
                val currentTime = Clock.System.now().toEpochMilliseconds()
                val existingRating = ratings[gameId]
                
                val userRating = if (existingRating != null) {
                    existingRating.copy(
                        rating = rating,
                        updatedAt = currentTime
                    )
                } else {
                    UserRating(
                        gameId = gameId,
                        rating = rating,
                        createdAt = currentTime,
                        updatedAt = currentTime
                    )
                }
                
                ratings[gameId] = userRating
                Result.success(Unit)
            }
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getUserRating(gameId: Int): Result<UserRating?> {
        return try {
            Result.success(ratings[gameId])
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun deleteUserRating(gameId: Int): Result<Unit> {
        return try {
            ratings.remove(gameId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getAllUserRatings(): Result<List<UserRating>> {
        return try {
            val allRatings = ratings.values.sortedByDescending { it.updatedAt }
            Result.success(allRatings)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun setUserReview(gameId: Int, reviewText: String): Result<Unit> {
        return try {
            when {
                reviewText.isEmpty() -> Result.failure(UserRatingReviewError.EmptyReview)
                reviewText.length > 1000 -> Result.failure(UserRatingReviewError.ReviewTooLong(reviewText.length))
                else -> {
                    val currentTime = Clock.System.now().toEpochMilliseconds()
                    val existingReview = reviews[gameId]
                    
                    val userReview = if (existingReview != null) {
                        existingReview.copy(
                            reviewText = reviewText,
                            updatedAt = currentTime
                        )
                    } else {
                        UserReview(
                            gameId = gameId,
                            reviewText = reviewText,
                            createdAt = currentTime,
                            updatedAt = currentTime
                        )
                    }
                    
                    reviews[gameId] = userReview
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getUserReview(gameId: Int): Result<UserReview?> {
        return try {
            Result.success(reviews[gameId])
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun deleteUserReview(gameId: Int): Result<Unit> {
        return try {
            reviews.remove(gameId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getAllUserReviews(): Result<List<UserReview>> {
        return try {
            val allReviews = reviews.values.sortedByDescending { it.updatedAt }
            Result.success(allReviews)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getUserRatingStats(): Result<UserRatingStats> {
        return try {
            val totalRatedGames = ratings.size
            val totalReviews = reviews.size
            val averageRating = if (ratings.isNotEmpty()) {
                ratings.values.map { it.rating }.average()
            } else {
                0.0
            }
            
            val ratingDistribution = mutableMapOf<Int, Int>()
            for (i in 1..5) {
                ratingDistribution[i] = ratings.values.count { it.rating == i }
            }
            
            val stats = UserRatingStats(
                totalRatedGames = totalRatedGames,
                totalReviews = totalReviews,
                averageRating = averageRating,
                ratingDistribution = ratingDistribution
            )
            
            Result.success(stats)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getGameWithUserData(gameId: Int): Result<GameWithUserData?> {
        return try {
            val userRating = ratings[gameId]
            val userReview = reviews[gameId]
            
            val gameWithUserData = GameWithUserData(
                gameId = gameId,
                userRating = userRating,
                userReview = userReview
            )
            
            Result.success(gameWithUserData)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getGamesWithUserData(gameIds: List<Int>): Result<List<GameWithUserData>> {
        return try {
            val gamesWithUserData = gameIds.map { gameId ->
                GameWithUserData(
                    gameId = gameId,
                    userRating = ratings[gameId],
                    userReview = reviews[gameId]
                )
            }
            
            Result.success(gamesWithUserData)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    override suspend fun getRecentUserActivity(limit: Int): Result<List<RecentActivity>> {
        return try {
            val recentRatings = ratings.values
                .sortedByDescending { it.updatedAt }
                .take(limit / 2)
                .map { rating ->
                    RecentActivity(
                        activityType = ActivityType.RATING,
                        gameId = rating.gameId,
                        gameName = "Game ${rating.gameId}", // Mock game name
                        gameImage = "https://example.com/game${rating.gameId}.jpg", // Mock image
                        rating = rating.rating,
                        reviewText = null,
                        activityDate = rating.updatedAt
                    )
                }
            
            val recentReviews = reviews.values
                .sortedByDescending { it.updatedAt }
                .take(limit / 2)
                .map { review ->
                    RecentActivity(
                        activityType = ActivityType.REVIEW,
                        gameId = review.gameId,
                        gameName = "Game ${review.gameId}", // Mock game name
                        gameImage = "https://example.com/game${review.gameId}.jpg", // Mock image
                        rating = null,
                        reviewText = review.reviewText.take(100), // Truncate for preview
                        activityDate = review.updatedAt
                    )
                }
            
            val allActivity = (recentRatings + recentReviews)
                .sortedByDescending { it.activityDate }
                .take(limit)
            
            Result.success(allActivity)
        } catch (e: Exception) {
            Result.failure(UserRatingReviewError.DatabaseError)
        }
    }
    
    /**
     * Helper method for testing data persistence scenarios
     */
    fun copyDataFrom(other: FakeUserRatingReviewRepository) {
        this.ratings.clear()
        this.reviews.clear()
        this.ratings.putAll(other.ratings)
        this.reviews.putAll(other.reviews)
    }
    
    /**
     * Helper method to simulate database errors for testing
     */
    fun simulateError(shouldError: Boolean = true) {
        // This could be used to test error scenarios
        // For now, we'll handle errors in individual methods
    }
    
    /**
     * Helper method to get current data size for testing
     */
    fun getDataSize(): Pair<Int, Int> {
        return Pair(ratings.size, reviews.size)
    }
    
    /**
     * Helper method to clear all data for testing
     */
    fun clearAllData() {
        ratings.clear()
        reviews.clear()
    }
}