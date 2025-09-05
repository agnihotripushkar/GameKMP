package com.devpush.features.game.data.repository

import com.devpush.coreDatabase.AppDatabase
import com.devpush.coreNetwork.apiService.ApiService
import com.devpush.features.game.data.mappers.toDomainGameDetails
import com.devpush.features.game.data.mappers.toDomainListOfGames
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.GameDetails
import com.devpush.features.game.domain.repository.GameRepository

class GameRepositoryImpl(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase
): GameRepository {
    override suspend fun getGames(): Result<List<Game>> {
        val result = apiService.getGames()
        return if (result.isSuccess) {
            Result.success(result.getOrThrow().results.toDomainListOfGames())
        }else{
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    override suspend fun getDetails(id: Int): Result<GameDetails> {
        val result = apiService.getDetails(id)
        return if (result.isSuccess) {
            Result.success(result.getOrThrow().toDomainGameDetails())
        }else{
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    override suspend fun save(id: Int, image: String, name: String) {
        appDatabase.appDatabaseQueries.upsert(id.toLong(), image, name)
    }

    override suspend fun delete(id: Int) {
        appDatabase.appDatabaseQueries.delete(id.toLong())
    }

}