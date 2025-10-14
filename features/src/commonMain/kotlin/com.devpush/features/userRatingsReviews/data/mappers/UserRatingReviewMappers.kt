package com.devpush.features.userRatingsReviews.data.mappers

import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.userRatingsReviews.domain.repository.RecentActivity
import com.devpush.features.userRatingsReviews.domain.repository.ActivityType
import com.devpush.features.game.domain.model.Game

// Note: These mapper functions will be implemented once SQLDelight generates the database classes
// For now, we'll create placeholder functions that can be used by the repository

/**
 * Creates a UserRating from database row data
 */
fun createUserRatingFromRow(
    gameId: Long,
    rating: Long,
    createdAt: Long,
    updatedAt: Long
): UserRating {
    return UserRating(
        gameId = gameId.toInt(),
        rating = rating.toInt(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Creates a UserReview from database row data
 */
fun createUserReviewFromRow(
    gameId: Long,
    reviewText: String,
    createdAt: Long,
    updatedAt: Long
): UserReview {
    return UserReview(
        gameId = gameId.toInt(),
        reviewText = reviewText,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

/**
 * Creates a GameWithUserData from database row data
 */
fun createGameWithUserDataFromRow(
    gameId: Long,
    gameName: String,
    gameImage: String,
    gameRating: Double,
    gameReleaseDate: String?,
    userRating: Long?,
    userRatingCreatedAt: Long?,
    userRatingUpdatedAt: Long?,
    userReview: String?,
    userReviewCreatedAt: Long?,
    userReviewUpdatedAt: Long?
): GameWithUserData {
    // Create the base Game object
    val game = Game(
        id = gameId.toInt(),
        name = gameName,
        imageUrl = gameImage,
        rating = gameRating,
        releaseDate = gameReleaseDate,
        platforms = emptyList(), // Will be populated separately if needed
        genres = emptyList() // Will be populated separately if needed
    )
    
    // Create UserRating if data exists
    val userRatingObj = if (userRating != null && userRatingCreatedAt != null && userRatingUpdatedAt != null) {
        UserRating(
            gameId = gameId.toInt(),
            rating = userRating.toInt(),
            createdAt = userRatingCreatedAt,
            updatedAt = userRatingUpdatedAt
        )
    } else null
    
    // Create UserReview if data exists
    val userReviewObj = if (userReview != null && userReviewCreatedAt != null && userReviewUpdatedAt != null) {
        UserReview(
            gameId = gameId.toInt(),
            reviewText = userReview,
            createdAt = userReviewCreatedAt,
            updatedAt = userReviewUpdatedAt
        )
    } else null
    
    return GameWithUserData(
        game = game,
        userRating = userRatingObj,
        userReview = userReviewObj
    )
}

/**
 * Creates a RecentActivity from database row data
 */
fun createRecentActivityFromRow(
    activityType: String,
    gameId: Long,
    gameName: String,
    gameImage: String,
    rating: Long?,
    reviewText: String?,
    activityDate: Long
): RecentActivity {
    return RecentActivity(
        activityType = when (activityType) {
            "rating" -> ActivityType.RATING
            "review" -> ActivityType.REVIEW
            else -> ActivityType.RATING // Default fallback
        },
        gameId = gameId.toInt(),
        gameName = gameName,
        gameImage = gameImage,
        rating = rating?.toInt(),
        reviewText = reviewText,
        activityDate = activityDate
    )
}

/**
 * Helper function to safely convert nullable Long to Int with validation
 */
fun Long?.toIntSafely(fieldName: String): Int {
    return this?.toInt() ?: throw IllegalArgumentException("$fieldName cannot be null")
}

/**
 * Helper function to validate and convert rating values
 */
fun Long.toValidRating(): Int {
    val rating = this.toInt()
    require(rating in 1..5) { "Rating must be between 1 and 5, got: $rating" }
    return rating
}

/**
 * Helper function to validate review text length
 */
fun String.validateReviewText(): String {
    require(this.isNotBlank()) { "Review text cannot be blank" }
    require(this.length <= 1000) { "Review text cannot exceed 1000 characters, got: ${this.length}" }
    return this
}