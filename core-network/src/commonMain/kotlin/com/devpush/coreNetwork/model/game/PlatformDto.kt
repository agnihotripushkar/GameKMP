package com.devpush.coreNetwork.model.game

import kotlinx.serialization.Serializable

@Serializable
data class PlatformDto(
    val id: Int,
    val name: String,
    val slug: String
)

@Serializable
data class PlatformWrapperDto(
    val platform: PlatformDto
)