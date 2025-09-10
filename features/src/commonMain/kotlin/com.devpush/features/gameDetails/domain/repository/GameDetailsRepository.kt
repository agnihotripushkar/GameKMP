package com.devpush.features.gameDetails.domain.repository

import com.devpush.features.gameDetails.domain.model.GameDetails

interface GameDetailsRepository {

    suspend fun getDetails(id: Int): Result<GameDetails>

    suspend fun save(id: Int,image: String,name: String)

    suspend fun delete(id: Int)

}