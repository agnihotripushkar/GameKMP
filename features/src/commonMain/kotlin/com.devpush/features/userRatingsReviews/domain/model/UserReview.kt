package com.devpush.features.userRatingsReviews.domain.model

/**
 * Domain model representing a user's personal review for a game
 * @param gameId The ID of the game being reviewed
 * @param reviewText The user's review text (max 1000 characters)
 * @param createdAt Timestamp when the review was first created
 * @param updatedAt Timestamp when the review was last updated
 */
data class UserReview(
    val gameId: Int,
    val reviewText: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    companion object {
        const val MAX_REVIEW_LENGTH = 1000
    }
    
    init {
        require(gameId > 0) { "Game ID must be positive, got: $gameId" }
        require(reviewText.isNotBlank()) { "Review text cannot be blank" }
        require(reviewText.length <= MAX_REVIEW_LENGTH) { 
            "Review text must be at most $MAX_REVIEW_LENGTH characters, got: ${reviewText.length}" 
        }
        require(createdAt > 0) { "Created timestamp must be positive, got: $createdAt" }
        require(updatedAt > 0) { "Updated timestamp must be positive, got: $updatedAt" }
        require(updatedAt >= createdAt) { "Updated timestamp must be >= created timestamp" }
    }
}