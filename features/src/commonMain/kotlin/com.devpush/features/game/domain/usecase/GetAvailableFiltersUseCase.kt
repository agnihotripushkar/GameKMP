package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre

data class FilterOptions(
    val platforms: List<Platform>,
    val genres: List<Genre>,
    val ratingRange: Pair<Double, Double>
)

interface GetAvailableFiltersUseCase {
    suspend operator fun invoke(games: List<Game>): FilterOptions
}

class GetAvailableFiltersUseCaseImpl : GetAvailableFiltersUseCase {
    override suspend fun invoke(games: List<Game>): FilterOptions {
        val allPlatforms = games.flatMap { it.platforms }
            .distinctBy { it.id }
            .sortedBy { it.name }
        
        val allGenres = games.flatMap { it.genres }
            .distinctBy { it.id }
            .sortedBy { it.name }
        
        val ratings = games.map { it.rating }.filter { it > 0.0 }
        val ratingRange = if (ratings.isNotEmpty()) {
            Pair(ratings.minOrNull() ?: 0.0, ratings.maxOrNull() ?: 5.0)
        } else {
            Pair(0.0, 5.0)
        }
        
        return FilterOptions(
            platforms = allPlatforms,
            genres = allGenres,
            ratingRange = ratingRange
        )
    }
}