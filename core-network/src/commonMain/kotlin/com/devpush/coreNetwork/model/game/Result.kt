package com.devpush.coreNetwork.model.game

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Result(
    val id: Int,
    val name: String,
    @SerialName("background_image")
    val backgroundImage: String?,
    val rating: Double = 0.0,
    @SerialName("released")
    val releaseDate: String? = null,
    val platforms: List<PlatformWrapperDto> = emptyList(),
    val genres: List<GenreDto> = emptyList()
)