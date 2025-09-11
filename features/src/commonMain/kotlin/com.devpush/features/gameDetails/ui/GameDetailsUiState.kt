package com.devpush.features.gameDetails.ui

import com.devpush.features.gameDetails.domain.model.GameDetails

data class GameDetailsUiState(
    val isLoading: Boolean = false,
    val error: String = "",
    val data: GameDetails? = null
)
