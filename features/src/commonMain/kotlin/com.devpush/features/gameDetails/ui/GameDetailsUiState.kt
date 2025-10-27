package com.devpush.features.gameDetails.ui

import com.devpush.features.gameDetails.domain.model.GameDetails
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview

/**
 * Represents the state of the FAB menu
 */
data class FABMenuState(
    val isExpanded: Boolean = false,
    val isAnimating: Boolean = false,
    val expandProgress: Float = 0f,
    val lastActionPerformed: FABAction? = null
)

/**
 * Represents different FAB actions available in the menu
 */
enum class FABAction {
    ADD_TO_COLLECTION,
    RATE_GAME,
    WRITE_REVIEW,
    SHARE_GAME
}

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
    val isReviewLoading: Boolean = false,
    val shouldFocusRating: Boolean = false,
    // FAB menu state
    val isFABMenuExpanded: Boolean = false,
    val fabMenuState: FABMenuState = FABMenuState(),
    val fabActionFeedback: String = ""
)
