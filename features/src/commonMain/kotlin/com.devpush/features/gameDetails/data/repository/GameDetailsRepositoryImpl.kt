package com.devpush.features.gameDetails.data.repository

import com.devpush.coreDatabase.AppDatabase
import com.devpush.coreNetwork.apiService.ApiService
import com.devpush.features.gameDetails.domain.model.GameDetails
import com.devpush.features.gameDetails.data.mappers.toDomainGameDetails
import com.devpush.features.gameDetails.domain.repository.GameDetailsRepository

class GameDetailsRepositoryImpl(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase
): GameDetailsRepository {

    override suspend fun getDetails(id: Int): Result<GameDetails> {
        val result = apiService.getDetails(id)
        return if (result.isSuccess) {
            Result.success(result.getOrThrow().toDomainGameDetails())
        }else{
            Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
        }
    }

    override suspend fun save(id: Int, image: String, name: String) {
        appDatabase.appDatabaseQueries.upsert(id.toLong(), name, image, 0.0, null)
    }

    override suspend fun delete(id: Int) {
        appDatabase.appDatabaseQueries.delete(id.toLong())
    }
}