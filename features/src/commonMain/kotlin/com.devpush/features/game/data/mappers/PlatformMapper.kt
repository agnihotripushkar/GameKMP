package com.devpush.features.game.data.mappers

import com.devpush.features.game.domain.model.Platform
import com.devpush.coreNetwork.model.game.PlatformDto
import com.devpush.coreNetwork.model.game.PlatformWrapperDto

fun PlatformDto.toDomain(): Platform = Platform(
    id = id,
    name = name,
    slug = slug
)

fun List<PlatformWrapperDto>.toDomainPlatforms(): List<Platform> = map { 
    it.platform.toDomain() 
}