package com.devpush.features.game.domain.model

data class Game(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val platforms: List<Platform> = emptyList(),
    val genres: List<Genre> = emptyList(),
    val rating: Double = 0.0,
    val releaseDate: String? = null
)
