package com.devpush.features.statistics.data.repository

import com.devpush.coreDatabase.AppDatabase
import com.devpush.features.statistics.domain.repository.UserRatingReviewRepository
import com.devpush.features.statistics.domain.repository.RecentActivity
import com.devpush.features.statistics.domain.repository.ActivityType
import com.devpush.features.statistics.domain.model.UserRating
import com.devpush.features.statistics.domain.model.UserReview
import com.devpush.features.statistics.domain.model.GameWithUserData
import com.devpush.features.statistics.domain.model.UserRatingStats
import com.devpush.features.statistics.domain.model.UserRatingReviewError
import com.devpush.features.statistics.domain.validation.UserRatingReviewValidator
import com.devpush.features.statistics.domain.validation.ValidationResult
import com.devpush.features.statistics.domain.validation.InputSanitizer
import com.devpush.features.statistics.data.mappers.createUserRatingFromRow
import com.devpush.features.statistics.data.mappers.createUserReviewFromRow
import com.devpush.features.statistics.data.mappers.createGameWithUserDataFromRow
import com.devpush.features.statistics.data.mappers.createRecentActivityFromRow
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.repository.GameRepository
import com.devpush.features.common.utils.SearchUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

/**
 * Implementation of UserRatingReviewRepository using SQLDelight database
 * Provides thread-safe operations for user ratings and reviews with comprehensive error handling
 * 
 * NOTE: This is currently a stub implementation. The actual database operations will be
 * implemented once the SQLDelight classes are properly generated and available.
 */
class UserRatingReviewRepositoryImpl(
    private val appDatabase: AppDatabase,
    private val gameRepository: com.devpush.features.game.domain.repository.GameRepository
) : UserRatingReviewRepository {
    
    // Thread-safe cache for performance optimization
    private var cachedRatings: Map<Int, UserRating>? = null
    private var cachedReviews: Map<Int, UserReview>? = null
    private var cacheTimestamp = 0L
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes
    private val cacheMutex = Mutex()
    
    // User Rating operations
    
    override suspend fun setUserRating(gameId: Int, rating: Int): Result<Unit> {
        return try {
            // Validate input parameters
            val validationResult = UserRatingReviewValidator.validateRating(rating)
            if (!validationResult.isValid) {
                return Result.failure((validationResult as ValidationResult.Error).error)
            }
            
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // Check if game exists in database
            val gameExists = checkGameExists(gameId)
            if (!gameExists) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            
            // TODO: Implement database operations once SQLDelight classes are generated
            // Check if rating already exists
            // val existingRating = appDatabase.appDatabaseQueries.getUserRating(gameId.toLong()).executeAsOneOrNull()
            
            // if (existingRating != null) {
            //     // Update existing rating
            //     appDatabase.appDatabaseQueries.updateUserRating(
            //         rating = rating.toLong(),
            //         updated_at = currentTime,
            //         game_id = gameId.toLong()
            //     )
            // } else {
            //     // Insert new rating
            //     appDatabase.appDatabaseQueries.insertUserRating(
            //         game_id = gameId.toLong(),
            //         rating = rating.toLong(),
            //         created_at = currentTime,
            //         updated_at = currentTime
            //     )
            // }
            
            // Invalidate cache
            invalidateCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getUserRating(gameId: Int): Result<UserRating?> {
        return try {
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // Check cache first
            cacheMutex.withLock {
                val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
                if (cachedRatings != null && (currentTime - cacheTimestamp) < cacheValidityDuration) {
                    return Result.success(cachedRatings!![gameId])
                }
            }
            
            // TODO: Implement once SQLDelight classes are generated
            // val ratingEntity = appDatabase.appDatabaseQueries.getUserRating(gameId.toLong()).executeAsOneOrNull()
            // val userRating = ratingEntity?.let { createUserRatingFromRow(it.game_id, it.rating, it.created_at, it.updated_at) }
            val userRating: UserRating? = null // Placeholder
            
            Result.success(userRating)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun deleteUserRating(gameId: Int): Result<Unit> {
        return try {
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // TODO: Implement database operations once SQLDelight classes are generated
            // Check if rating exists
            // val existingRating = appDatabase.appDatabaseQueries.getUserRating(gameId.toLong()).executeAsOneOrNull()
            // if (existingRating == null) {
            //     return Result.failure(UserRatingReviewError.RatingNotFound)
            // }
            // 
            // appDatabase.appDatabaseQueries.deleteUserRating(gameId.toLong())
            
            // Invalidate cache
            invalidateCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getAllUserRatings(): Result<List<UserRating>> {
        return try {
            // TODO: Implement database operations once SQLDelight classes are generated
            // val ratingEntities = appDatabase.appDatabaseQueries.getAllUserRatings().executeAsList()
            // val userRatings = ratingEntities.map { createUserRatingFromRow(it.game_id, it.rating, it.created_at, it.updated_at) }
            val userRatings = emptyList<UserRating>() // Placeholder
            
            // Update cache
            cacheMutex.withLock {
                cachedRatings = userRatings.associateBy { it.gameId }
                cacheTimestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            }
            
            Result.success(userRatings)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    // User Review operations
    
    override suspend fun setUserReview(gameId: Int, reviewText: String): Result<Unit> {
        return try {
            // Validate and sanitize input
            val sanitizedText = InputSanitizer.sanitizeReviewText(reviewText)
            val validationResult = UserRatingReviewValidator.validateReviewText(sanitizedText)
            if (!validationResult.isValid) {
                return Result.failure((validationResult as ValidationResult.Error).error)
            }
            
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // Check if game exists in database
            val gameExists = checkGameExists(gameId)
            if (!gameExists) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            val currentTime = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            
            // TODO: Implement database operations once SQLDelight classes are generated
            // Check if review already exists
            // val existingReview = appDatabase.appDatabaseQueries.getUserReview(gameId.toLong()).executeAsOneOrNull()
            // 
            // if (existingReview != null) {
            //     // Update existing review
            //     appDatabase.appDatabaseQueries.updateUserReview(
            //         review_text = sanitizedText,
            //         updated_at = currentTime,
            //         game_id = gameId.toLong()
            //     )
            // } else {
            //     // Insert new review
            //     appDatabase.appDatabaseQueries.insertUserReview(
            //         game_id = gameId.toLong(),
            //         review_text = sanitizedText,
            //         created_at = currentTime,
            //         updated_at = currentTime
            //     )
            // }
            
            // Invalidate cache
            invalidateCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getUserReview(gameId: Int): Result<UserReview?> {
        return try {
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // TODO: Implement database operations once SQLDelight classes are generated
            val userReview: UserReview? = null // Placeholder
            
            Result.success(userReview)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun deleteUserReview(gameId: Int): Result<Unit> {
        return try {
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // TODO: Implement database operations once SQLDelight classes are generated
            // Invalidate cache
            invalidateCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getAllUserReviews(): Result<List<UserReview>> {
        return try {
            // TODO: Implement database operations once SQLDelight classes are generated
            val userReviews = emptyList<UserReview>() // Placeholder
            
            // Update cache
            cacheMutex.withLock {
                cachedReviews = userReviews.associateBy { it.gameId }
                cacheTimestamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
            }
            
            Result.success(userReviews)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    // Statistics operations
    
    override suspend fun getUserRatingStats(): Result<UserRatingStats> {
        return try {
            // TODO: Implement database operations once SQLDelight classes are generated
            // Placeholder implementation
            val userRatingStats = UserRatingStats(
                totalRatedGames = 0,
                totalReviews = 0,
                averageRating = 0.0,
                ratingDistribution = (1..5).associateWith { 0 }
            )
            
            Result.success(userRatingStats)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    // Combined data operations
    
    override suspend fun getGameWithUserData(gameId: Int): Result<GameWithUserData?> {
        return try {
            if (gameId <= 0) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // TODO: Implement database operations once SQLDelight classes are generated
            val gameWithUserData: GameWithUserData? = null // Placeholder
            
            Result.success(gameWithUserData)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getGamesWithUserData(gameIds: List<Int>): Result<List<GameWithUserData>> {
        return try {
            if (gameIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Validate game IDs
            val validGameIds = gameIds.filter { it > 0 }
            if (validGameIds.isEmpty()) {
                return Result.failure(UserRatingReviewError.GameNotFound)
            }
            
            // Get all games from the game repository
            val allGamesResult = gameRepository.getGames()
            val allGames = allGamesResult.getOrElse { exception ->
                return Result.failure(
                    UserRatingReviewError.UnknownError(
                        "Failed to load games: ${exception.message}",
                        exception
                    )
                )
            }
            
            // Filter games by the requested IDs
            val requestedGames = allGames.filter { game -> 
                validGameIds.contains(game.id) 
            }
            
            // TODO: Load actual user ratings and reviews from database once SQLDelight is implemented
            // For now, create GameWithUserData objects with null user data
            val gamesWithUserData = requestedGames.map { game ->
                GameWithUserData(
                    game = game,
                    userRating = null, // TODO: Load from database
                    userReview = null  // TODO: Load from database
                )
            }
            
            Result.success(gamesWithUserData)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    override suspend fun getRecentUserActivity(limit: Int): Result<List<RecentActivity>> {
        return try {
            val validLimit = limit.coerceIn(1, 100) // Ensure reasonable limit
            
            // TODO: Implement database operations once SQLDelight classes are generated
            val recentActivities = emptyList<RecentActivity>() // Placeholder
            
            Result.success(recentActivities)
        } catch (e: Exception) {
            handleDatabaseException(e)
        }
    }
    
    // Private helper methods
    
    private suspend fun checkGameExists(gameId: Int): Boolean {
        return try {
            // TODO: Implement database operations once SQLDelight classes are generated
            // val game = appDatabase.appDatabaseQueries.getAllGames().executeAsList()
            //     .find { it.id == gameId.toLong() }
            // game != null
            true // Placeholder - assume game exists
        } catch (e: Exception) {
            false
        }
    }
    
    private suspend fun invalidateCache() {
        cacheMutex.withLock {
            cachedRatings = null
            cachedReviews = null
            cacheTimestamp = 0L
        }
    }
    
    private fun <T> handleDatabaseException(exception: Exception): Result<T> {
        return when {
            exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("FOREIGN KEY constraint failed") } } == true -> {
                Result.failure(UserRatingReviewError.GameNotFound)
            }
            exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("CHECK constraint failed") } } == true -> {
                when {
                    exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("rating") } } == true -> {
                        Result.failure(UserRatingReviewError.InvalidRating(0))
                    }
                    exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("review_text") } } == true -> {
                        Result.failure(UserRatingReviewError.ReviewTooLong(0))
                    }
                    else -> Result.failure(UserRatingReviewError.DatabaseError)
                }
            }
            exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("UNIQUE constraint failed") } } == true -> {
                Result.failure(UserRatingReviewError.DatabaseError)
            }
            exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("database") } } == true -> {
                Result.failure(UserRatingReviewError.DatabaseError)
            }
            exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("network") } } == true -> {
                Result.failure(UserRatingReviewError.NetworkError)
            }
            else -> {
                Result.failure(UserRatingReviewError.UnknownError(
                    exception.message ?: "Unknown database error",
                    exception
                ))
            }
        }
    }
}