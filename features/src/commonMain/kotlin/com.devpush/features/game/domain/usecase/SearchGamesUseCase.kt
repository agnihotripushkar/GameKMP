package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.Game
import com.devpush.features.common.utils.StringUtils
import com.devpush.features.common.utils.SearchUtils

interface SearchGamesUseCase {
    suspend operator fun invoke(
        games: List<Game>,
        query: String
    ): List<Game>
}

class SearchGamesUseCaseImpl : SearchGamesUseCase {
    override suspend fun invoke(
        games: List<Game>,
        query: String
    ): List<Game> {
        if (query.isBlank()) {
            return games
        }
        
        val searchQuery = query.trim()
        return games.filter { game ->
            with(SearchUtils) {
                game.name.containsIgnoreCase(searchQuery)
            }
        }
    }
}