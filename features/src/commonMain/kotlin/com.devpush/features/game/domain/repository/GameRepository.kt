package com.devpush.features.game.domain.repository

import com.devpush.features.game.domain.model.Game

interface GameRepository {

    suspend fun getGames():Result<List<Game>>

    suspend fun save(id: Int,image: String,name: String)

    suspend fun delete(id: Int)

}