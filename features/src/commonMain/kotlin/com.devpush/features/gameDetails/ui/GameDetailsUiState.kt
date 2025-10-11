package com.devpush.features.gameDetails.ui

import com.devpush.features.gameDetails.domain.model.GameDetails
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionType

data class GameDetailsUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val data: GameDetails? = null,
    // Collection-related state
    val collections: List<GameCollection> = emptyList(),
    val gameCollectionTypes: List<CollectionType> = emptyList(),
    val showAddToCollectionDialog: Boolean = false,
    val isCollectionsLoading: Boolean = false
)
