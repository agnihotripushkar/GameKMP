package com.devpush.features.game.data.mappers

import com.devpush.features.game.domain.model.Game

fun List<com.devpush.coreNetwork.model.game.Result>.toDomainListOfGames(): List<Game> = map {
    Game(
        id = it.id,
        name = it.name,
        imageUrl = it.background_image
    )
}