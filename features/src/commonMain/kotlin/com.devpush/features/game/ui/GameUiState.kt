package com.devpush.features.game.ui

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.SearchFilterError

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
    val canRetry: Boolean = false
)
