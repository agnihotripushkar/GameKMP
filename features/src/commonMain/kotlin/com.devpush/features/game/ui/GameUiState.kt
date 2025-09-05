package com.devpush.features.game.ui

import com.devpush.features.game.domain.model.Game

data class GameUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
