package com.devpush.coreNetwork.model.game

import kotlinx.serialization.Serializable

@Serializable
data class GenreDto(
    val id: Int,
    val name: String,
    val slug: String
)