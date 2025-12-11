package com.devpush.features.statistics.domain.repository

import com.devpush.features.statistics.domain.model.UserRating
import com.devpush.features.statistics.domain.model.UserReview
import com.devpush.features.statistics.domain.model.GameWithUserData
import com.devpush.features.statistics.domain.model.UserRatingStats

/**
 * Repository interface for managing user ratings and reviews
 * Provides methods for CRUD operations on user ratings and reviews,
 * as well as statistics and combined data retrieval
 */
interface UserRatingReviewRepository {
    
    // User Rating operations
    
    /**
     * Sets or updates a user's rating for a game
     * @param gameId The ID of the game to rate
     * @param rating The rating value (1-5 stars)
     * @return Result indicating success or failure
     */
    suspend fun setUserRating(gameId: Int, rating: Int): Result<Unit>
    
    /**
     * Retrieves a user's rating for a specific game
     * @param gameId The ID of the game
     * @return Result containing the UserRating or null if not found
     */
    suspend fun getUserRating(gameId: Int): Result<UserRating?>
    
    /**
     * Deletes a user's rating for a specific game
     * @param gameId The ID of the game
     * @return Result indicating success or failure
     */
    suspend fun deleteUserRating(gameId: Int): Result<Unit>
    
    /**
     * Retrieves all user ratings ordered by most recently updated
     * @return Result containing list of all UserRating objects
     */
    suspend fun getAllUserRatings(): Result<List<UserRating>>
    
    // User Review operations
    
    /**
     * Sets or updates a user's review for a game
     * @param gameId The ID of the game to review
     * @param reviewText The review text (max 1000 characters)
     * @return Result indicating success or failure
     */
    suspend fun setUserReview(gameId: Int, reviewText: String): Result<Unit>
    
    /**
     * Retrieves a user's review for a specific game
     * @param gameId The ID of the game
     * @return Result containing the UserReview or null if not found
     */
    suspend fun getUserReview(gameId: Int): Result<UserReview?>
    
    /**
     * Deletes a user's review for a specific game
     * @param gameId The ID of the game
     * @return Result indicating success or failure
     */
    suspend fun deleteUserReview(gameId: Int): Result<Unit>
    
    /**
     * Retrieves all user reviews ordered by most recently updated
     * @return Result containing list of all UserReview objects
     */
    suspend fun getAllUserReviews(): Result<List<UserReview>>
    
    // Statistics operations
    
    /**
     * Retrieves aggregated statistics about user ratings and reviews
     * @return Result containing UserRatingStats with totals, averages, and distribution
     */
    suspend fun getUserRatingStats(): Result<UserRatingStats>
    
    // Combined data operations
    
    /**
     * Retrieves a game with its associated user rating and review data
     * @param gameId The ID of the game
     * @return Result containing GameWithUserData or null if game not found
     */
    suspend fun getGameWithUserData(gameId: Int): Result<GameWithUserData?>
    
    /**
     * Retrieves multiple games with their associated user rating and review data
     * @param gameIds List of game IDs to retrieve
     * @return Result containing list of GameWithUserData objects
     */
    suspend fun getGamesWithUserData(gameIds: List<Int>): Result<List<GameWithUserData>>
    
    /**
     * Retrieves recent user activity (ratings and reviews) for display
     * @param limit Maximum number of activities to return
     * @return Result containing list of recent activities with game information
     */
    suspend fun getRecentUserActivity(limit: Int = 10): Result<List<RecentActivity>>
}

/**
 * Data class representing recent user activity for display purposes
 */
data class RecentActivity(
    val activityType: ActivityType,
    val gameId: Int,
    val gameName: String,
    val gameImage: String,
    val rating: Int? = null,
    val reviewText: String? = null,
    val activityDate: Long
)

/**
 * Enum representing the type of user activity
 */
enum class ActivityType {
    RATING,
    REVIEW
}