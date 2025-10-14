package com.devpush.features.gameDetails.ui

import com.devpush.features.gameDetails.domain.model.GameDetails
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview

data class GameDetailsUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val data: GameDetails? = null,
    // Collection-related state
    val collections: List<GameCollection> = emptyList(),
    val gameCollectionTypes: List<CollectionType> = emptyList(),
    val showAddToCollectionDialog: Boolean = false,
    val isCollectionsLoading: Boolean = false,
    // User rating and review state
    val userRating: UserRating? = null,
    val userReview: UserReview? = null,
    val isUserDataLoading: Boolean = false,
    val userDataError: String = "",
    val showReviewDialog: Boolean = false,
    val isRatingLoading: Boolean = false,
    val isReviewLoading: Boolean = false
)
