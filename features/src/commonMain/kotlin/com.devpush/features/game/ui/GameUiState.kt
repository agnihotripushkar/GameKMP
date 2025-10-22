package com.devpush.features.game.ui

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.SearchFilterError
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.bookmarklist.domain.collections.CollectionType

data class GameUiState(
    val games: List<Game> = emptyList(),
    val filteredGames: List<Game> = emptyList(),
    val availablePlatforms: List<Platform> = emptyList(),
    val availableGenres: List<Genre> = emptyList(),
    val searchFilterState: SearchFilterState = SearchFilterState(),
    val isLoading: Boolean = false,
    val isFilterLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val filterError: SearchFilterError? = null,
    val canRetry: Boolean = false,
    // Collection-related state
    val collections: List<GameCollection> = emptyList(),
    val gameCollectionMap: Map<Int, List<CollectionType>> = emptyMap(),
    val showAddToCollectionDialog: Boolean = false,
    val selectedGameForCollection: Game? = null,
    val isCollectionsLoading: Boolean = false
)
