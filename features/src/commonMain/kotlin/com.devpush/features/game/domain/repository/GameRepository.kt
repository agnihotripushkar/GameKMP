package com.devpush.features.game.domain.repository

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState

interface GameRepository {

    suspend fun getGames(): Result<List<Game>>
    
    suspend fun getGames(searchFilterState: SearchFilterState): Result<List<Game>>

    suspend fun searchGames(query: String): Result<List<Game>>
    
    suspend fun getAvailablePlatforms(): Result<List<Platform>>
    
    suspend fun getAvailableGenres(): Result<List<Genre>>

    suspend fun save(id: Int, image: String, name: String)

    suspend fun delete(id: Int)

}