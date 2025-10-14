package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre

interface FilterGamesUseCase {
    suspend operator fun invoke(
        games: List<Game>,
        platforms: Set<Platform>,
        genres: Set<Genre>,
        minRating: Double
    ): List<Game>
}

class FilterGamesUseCaseImpl : FilterGamesUseCase {
    override suspend fun invoke(
        games: List<Game>,
        platforms: Set<Platform>,
        genres: Set<Genre>,
        minRating: Double
    ): List<Game> {
        return games.filter { game ->
            val matchesPlatforms = if (platforms.isEmpty()) {
                true
            } else {
                game.platforms.any { gamePlatform ->
                    platforms.any { selectedPlatform ->
                        selectedPlatform.id == gamePlatform.id
                    }
                }
            }
            
            val matchesGenres = if (genres.isEmpty()) {
                true
            } else {
                game.genres.any { gameGenre ->
                    genres.any { selectedGenre ->
                        selectedGenre.id == gameGenre.id
                    }
                }
            }
            
            val matchesRating = game.rating >= minRating
            
            matchesPlatforms && matchesGenres && matchesRating
        }
    }
}