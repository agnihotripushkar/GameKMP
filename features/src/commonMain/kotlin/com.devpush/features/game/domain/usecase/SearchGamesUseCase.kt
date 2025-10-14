package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.Game

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
        
        val searchQuery = query.trim().lowercase()
        return games.filter { game ->
            game.name.lowercase().contains(searchQuery)
        }
    }
}