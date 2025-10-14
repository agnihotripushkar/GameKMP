package com.devpush.features.userRatingsReviews.domain.model

/**
 * Domain model representing a user's personal rating for a game
 * @param gameId The ID of the game being rated
 * @param rating The user's rating (1-5 stars)
 * @param createdAt Timestamp when the rating was first created
 * @param updatedAt Timestamp when the rating was last updated
 */
data class UserRating(
    val gameId: Int,
    val rating: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    init {
        require(rating in 1..5) { "Rating must be between 1 and 5 stars, got: $rating" }
        require(gameId > 0) { "Game ID must be positive, got: $gameId" }
        require(createdAt > 0) { "Created timestamp must be positive, got: $createdAt" }
        require(updatedAt > 0) { "Updated timestamp must be positive, got: $updatedAt" }
        require(updatedAt >= createdAt) { "Updated timestamp must be >= created timestamp" }
    }
}