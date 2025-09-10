package com.devpush.features.gameDetails.data.mappers

import com.devpush.coreNetwork.model.gameDetails.GameDetailsResponse
import com.devpush.features.gameDetails.domain.model.Developer
import com.devpush.features.gameDetails.domain.model.GameDetails
import com.devpush.features.gameDetails.domain.model.Platform
import com.devpush.features.gameDetails.domain.model.Store
import com.devpush.features.gameDetails.domain.model.Tag

fun GameDetailsResponse.toDomainGameDetails(): GameDetails {
    return GameDetails(
        name = name,
        id = id,
        description = description_raw,
        backgroundImage = background_image,
        additionalImage = background_image_additional,
        platforms = platforms.map {
            Platform(
                name = it.platform.name,
                image = it.platform.image_background
            )
        },
        stores = stores.map {
            Store(
                name = it.store.name,
                image = it.store.image_background,
                gameCount = it.store.games_count,
                domain = it.store.domain
            )
        },
        developers = developers.map {
            Developer(
                name = it.name,
                image = it.image_background,
                gameCount = it.games_count
            )
        },
        tags = tags.map {
            Tag(
                name = it.name,
                image = it.image_background
            )
        }
    )
}