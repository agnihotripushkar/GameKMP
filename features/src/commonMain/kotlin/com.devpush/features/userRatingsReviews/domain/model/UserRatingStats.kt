package com.devpush.features.userRatingsReviews.domain.model

/**
 * Domain model representing aggregated statistics about a user's ratings and reviews
 * @param totalRatedGames Total number of games the user has rated
 * @param totalReviews Total number of reviews the user has written
 * @param averageRating Average rating across all rated games
 * @param ratingDistribution Map of rating value to count (e.g., 5 -> 10 means 10 five-star ratings)
 */
data class UserRatingStats(
    val totalRatedGames: Int,
    val totalReviews: Int,
    val averageRating: Double,
    val ratingDistribution: Map<Int, Int>
) {
    init {
        require(totalRatedGames >= 0) { "Total rated games must be non-negative, got: $totalRatedGames" }
        require(totalReviews >= 0) { "Total reviews must be non-negative, got: $totalReviews" }
        require(averageRating >= 0.0) { "Average rating must be non-negative, got: $averageRating" }
        require(if (totalRatedGames > 0) averageRating in 1.0..5.0 else averageRating == 0.0) {
            "Average rating must be between 1.0 and 5.0 when games are rated, got: $averageRating"
        }
        require(ratingDistribution.keys.all { it in 1..5 }) {
            "Rating distribution keys must be between 1 and 5, got: ${ratingDistribution.keys}"
        }
        require(ratingDistribution.values.all { it >= 0 }) {
            "Rating distribution values must be non-negative, got: ${ratingDistribution.values}"
        }
        require(ratingDistribution.values.sum() == totalRatedGames) {
            "Rating distribution sum (${ratingDistribution.values.sum()}) must equal total rated games ($totalRatedGames)"
        }
    }
    
    /**
     * Returns the percentage of games rated with the specified rating
     */
    fun getPercentageForRating(rating: Int): Double {
        require(rating in 1..5) { "Rating must be between 1 and 5, got: $rating" }
        return if (totalRatedGames == 0) 0.0 else {
            (ratingDistribution[rating] ?: 0) * 100.0 / totalRatedGames
        }
    }
    
    /**
     * Returns the most common rating given by the user
     */
    fun getMostCommonRating(): Int? {
        return ratingDistribution.maxByOrNull { it.value }?.key
    }
}