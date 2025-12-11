package com.devpush.features.statistics.domain.model

import com.devpush.features.game.domain.model.Game

/**
 * Composite domain model that combines game information with user's personal data
 * @param game The base game information
 * @param userRating The user's personal rating for this game (null if not rated)
 * @param userReview The user's personal review for this game (null if no review)
 */
data class GameWithUserData(
    val game: Game,
    val userRating: UserRating? = null,
    val userReview: UserReview? = null
) {
    /**
     * Indicates whether the user has rated this game
     */
    val hasUserRating: Boolean
        get() = userRating != null
    
    /**
     * Indicates whether the user has written a review for this game
     */
    val hasUserReview: Boolean
        get() = userReview != null
    
    /**
     * Indicates whether the user has any personal data for this game
     */
    val hasUserData: Boolean
        get() = hasUserRating || hasUserReview
}