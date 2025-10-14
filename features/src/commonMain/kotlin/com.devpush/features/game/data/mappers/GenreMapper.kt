package com.devpush.features.game.data.mappers

import com.devpush.features.game.domain.model.Genre
import com.devpush.coreNetwork.model.game.GenreDto

fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name,
    slug = slug
)

fun List<GenreDto>.toDomainGenres(): List<Genre> = map { 
    it.toDomain() 
}