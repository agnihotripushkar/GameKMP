package com.devpush.features.game.data.repository

import com.devpush.coreDatabase.AppDatabase
import com.devpush.features.game.domain.model.Game
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.bookmarklist.domain.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository
import com.devpush.features.game.data.mappers.toDomain
import com.devpush.features.game.data.mappers.toDomainCollections
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class GameCollectionRepositoryImpl(
    private val appDatabase: AppDatabase
) : GameCollectionRepository {
    
    // Thread-safe cache for collections
    private var cachedCollections: List<GameCollection>? = null
    private var cacheTimestamp = 0L
    private val cacheValidityDuration = 2 * 60 * 1000L // 2 minutes
    private val cacheMutex = Mutex()
    
    // Collection game counts cache
    private var cachedGameCounts: Map<String, Int>? = null
    private var gameCountsCacheTimestamp = 0L
    
    override suspend fun createCollection(
        name: String, 
        type: CollectionType, 
        description: String?
    ): Result<GameCollection> {
        return try {
            // Validate collection name
            if (name.isBlank()) {
                return Result.failure(
                    CollectionError.ValidationError("collection name", "cannot be empty")
                )
            }
            
            if (name.length < 2 || name.length > 50) {
                return Result.failure(
                    CollectionError.ValidationError("collection name", "must be between 2 and 50 characters")
                )
            }
            
            // Check if name already exists
            val nameExistsResult = collectionNameExists(name)
            if (nameExistsResult.isFailure) {
                return Result.failure(nameExistsResult.exceptionOrNull() ?: CollectionError.DatabaseError)
            }
            
            if (nameExistsResult.getOrThrow()) {
                return Result.failure(CollectionError.CollectionNameExists(name))
            }
            
            // Validate description if provided
            if (description != null && description.length > 200) {
                return Result.failure(
                    CollectionError.ValidationError("description", "cannot exceed 200 characters")
                )
            }
            
            // Create new collection
            val currentTime = System.currentTimeMillis()
            val collection = GameCollection(
                id = Uuid.random().toString(),
                name = name,
                type = type,
                gameIds = emptyList(),
                createdAt = currentTime,
                updatedAt = currentTime,
                description = description
            )
            
            // Validate the collection
            val validationResult = collection.validate()
            if (validationResult.isInvalid()) {
                return Result.failure(
                    CollectionError.ValidationError("collection", validationResult.getErrorMessage() ?: "Invalid collection")
                )
            }
            
            // Insert into database
            appDatabase.appDatabaseQueries.createCollection(
                collection.id,
                collection.name,
                collection.type.name,
                collection.description,
                collection.createdAt,
                collection.updatedAt
            )
            
            // Clear cache
            clearCache()
            
            Result.success(collection)
        } catch (e: Exception) {
            when {
                e.message?.contains("UNIQUE constraint failed", ignoreCase = true) == true -> {
                    Result.failure(CollectionError.CollectionNameExists(name))
                }
                e.message?.contains("constraint", ignoreCase = true) == true -> {
                    Result.failure(CollectionError.DatabaseConstraintError("collection creation"))
                }
                else -> {
                    Result.failure(CollectionError.DatabaseError)
                }
            }
        }
    }
    
    override suspend fun getAllCollections(): Result<List<GameCollection>> {
        return try {
            cacheMutex.withLock {
                // Check cache validity
                val currentTime = System.currentTimeMillis()
                if (cachedCollections != null && (currentTime - cacheTimestamp) < cacheValidityDuration) {
                    return@withLock Result.success(cachedCollections!!)
                }
                
                // Fetch from database
                val dbCollections = appDatabase.appDatabaseQueries.getAllCollections().executeAsList()
                val collections = dbCollections.toDomainCollections()
                
                // Populate game IDs for each collection
                val collectionsWithGames = collections.map { collection ->
                    val gameIds = getGameIdsInCollection(collection.id)
                    collection.copy(gameIds = gameIds)
                }
                
                // Update cache
                cachedCollections = collectionsWithGames
                cacheTimestamp = currentTime
                
                Result.success(collectionsWithGames)
            }
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun getCollectionById(id: String): Result<GameCollection> {
        return try {
            val dbCollection = appDatabase.appDatabaseQueries.getCollectionById(id).executeAsOneOrNull()
                ?: return Result.failure(CollectionError.CollectionNotFound(id))
            
            val collection = dbCollection.toDomain()
            val gameIds = getGameIdsInCollection(id)
            val collectionWithGames = collection.copy(gameIds = gameIds)
            
            Result.success(collectionWithGames)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun getCollectionsByType(type: CollectionType): Result<List<GameCollection>> {
        return try {
            val dbCollections = appDatabase.appDatabaseQueries.getCollectionsByType(type.name).executeAsList()
            val collections = dbCollections.toDomainCollections()
            
            // Populate game IDs for each collection
            val collectionsWithGames = collections.map { collection ->
                val gameIds = getGameIdsInCollection(collection.id)
                collection.copy(gameIds = gameIds)
            }
            
            Result.success(collectionsWithGames)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun addGameToCollection(collectionId: String, gameId: Int): Result<Unit> {
        return try {
            // Check if collection exists
            val collectionExists = appDatabase.appDatabaseQueries.getCollectionById(collectionId).executeAsOneOrNull()
            if (collectionExists == null) {
                return Result.failure(CollectionError.CollectionNotFound(collectionId))
            }
            
            // Check if game is already in collection
            val gameExists = appDatabase.appDatabaseQueries.checkGameInCollection(collectionId, gameId.toLong()).executeAsOne()
            if (gameExists == 1L) {
                return Result.failure(CollectionError.DuplicateGameInCollection(gameId, collectionExists.name))
            }
            
            // Add game to collection
            val currentTime = System.currentTimeMillis()
            appDatabase.appDatabaseQueries.addGameToCollection(collectionId, gameId.toLong(), currentTime)
            
            // Update collection timestamp
            appDatabase.appDatabaseQueries.updateCollection(
                collectionExists.name,
                collectionExists.description,
                currentTime,
                collectionId
            )
            
            // Clear cache
            clearCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            when {
                e.message?.contains("FOREIGN KEY constraint failed", ignoreCase = true) == true -> {
                    Result.failure(CollectionError.DatabaseConstraintError("foreign key"))
                }
                else -> {
                    Result.failure(CollectionError.DatabaseError)
                }
            }
        }
    }
    
    override suspend fun removeGameFromCollection(collectionId: String, gameId: Int): Result<Unit> {
        return try {
            // Check if collection exists
            val collectionExists = appDatabase.appDatabaseQueries.getCollectionById(collectionId).executeAsOneOrNull()
            if (collectionExists == null) {
                return Result.failure(CollectionError.CollectionNotFound(collectionId))
            }
            
            // Check if game is in collection
            val gameExists = appDatabase.appDatabaseQueries.checkGameInCollection(collectionId, gameId.toLong()).executeAsOne()
            if (gameExists == 0L) {
                return Result.failure(CollectionError.GameNotInCollection(gameId, collectionExists.name))
            }
            
            // Remove game from collection
            appDatabase.appDatabaseQueries.removeGameFromCollection(collectionId, gameId.toLong())
            
            // Update collection timestamp
            val currentTime = System.currentTimeMillis()
            appDatabase.appDatabaseQueries.updateCollection(
                collectionExists.name,
                collectionExists.description,
                currentTime,
                collectionId
            )
            
            // Clear cache
            clearCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun deleteCollection(id: String): Result<Unit> {
        return try {
            // Check if collection exists
            val collection = appDatabase.appDatabaseQueries.getCollectionById(id).executeAsOneOrNull()
            if (collection == null) {
                return Result.failure(CollectionError.CollectionNotFound(id))
            }
            
            // Check if it's a default collection (protect default collections)
            val collectionType = CollectionType.valueOf(collection.type)
            if (collectionType.isDefault) {
                return Result.failure(CollectionError.DefaultCollectionProtected(collectionType, "delete"))
            }
            
            // Delete collection (cascade will handle collection_game relationships)
            appDatabase.appDatabaseQueries.deleteCollection(id)
            
            // Clear cache
            clearCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun updateCollection(collection: GameCollection): Result<GameCollection> {
        return try {
            // Validate the collection
            val validationResult = collection.validate()
            if (validationResult.isInvalid()) {
                return Result.failure(
                    CollectionError.ValidationError("collection", validationResult.getErrorMessage() ?: "Invalid collection")
                )
            }
            
            // Check if collection exists
            val existingCollection = appDatabase.appDatabaseQueries.getCollectionById(collection.id).executeAsOneOrNull()
            if (existingCollection == null) {
                return Result.failure(CollectionError.CollectionNotFound(collection.id))
            }
            
            // Check if it's a default collection (protect default collections from name changes)
            val existingType = CollectionType.valueOf(existingCollection.type)
            if (existingType.isDefault && existingCollection.name != collection.name) {
                return Result.failure(CollectionError.DefaultCollectionProtected(existingType, "rename"))
            }
            
            // Check if new name already exists (excluding current collection)
            if (existingCollection.name != collection.name) {
                val nameExistsResult = collectionNameExists(collection.name, collection.id)
                if (nameExistsResult.isFailure) {
                    return Result.failure(nameExistsResult.exceptionOrNull() ?: CollectionError.DatabaseError)
                }
                
                if (nameExistsResult.getOrThrow()) {
                    return Result.failure(CollectionError.CollectionNameExists(collection.name))
                }
            }
            
            // Update collection
            val updatedCollection = collection.withUpdatedTimestamp()
            appDatabase.appDatabaseQueries.updateCollection(
                updatedCollection.name,
                updatedCollection.description,
                updatedCollection.updatedAt,
                updatedCollection.id
            )
            
            // Clear cache
            clearCache()
            
            Result.success(updatedCollection)
        } catch (e: Exception) {
            when {
                e.message?.contains("UNIQUE constraint failed", ignoreCase = true) == true -> {
                    Result.failure(CollectionError.CollectionNameExists(collection.name))
                }
                else -> {
                    Result.failure(CollectionError.DatabaseError)
                }
            }
        }
    }
    
    override suspend fun getGamesInCollection(collectionId: String): Result<List<Game>> {
        return try {
            // Check if collection exists
            val collectionExists = appDatabase.appDatabaseQueries.getCollectionById(collectionId).executeAsOneOrNull()
            if (collectionExists == null) {
                return Result.failure(CollectionError.CollectionNotFound(collectionId))
            }
            
            // Get games in collection
            val gamesInCollection = appDatabase.appDatabaseQueries.getGamesInCollection(collectionId).executeAsList()
            
            // Convert to domain models
            val games = gamesInCollection.map { gameWithAddedAt ->
                Game(
                    id = gameWithAddedAt.id.toInt(),
                    name = gameWithAddedAt.name,
                    imageUrl = gameWithAddedAt.image,
                    rating = gameWithAddedAt.rating,
                    releaseDate = gameWithAddedAt.release_date,
                    platforms = emptyList(), // Would need to fetch separately if needed
                    genres = emptyList() // Would need to fetch separately if needed
                )
            }
            
            Result.success(games)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun getCollectionsContainingGame(gameId: Int): Result<List<String>> {
        return try {
            val collections = appDatabase.appDatabaseQueries.getCollectionsByGameId(gameId.toLong()).executeAsList()
            val collectionIds = collections.map { it.id }
            Result.success(collectionIds)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun moveGameBetweenCollections(
        gameId: Int, 
        fromCollectionId: String, 
        toCollectionId: String
    ): Result<Unit> {
        return try {
            // Remove from source collection
            val removeResult = removeGameFromCollection(fromCollectionId, gameId)
            if (removeResult.isFailure) {
                return removeResult
            }
            
            // Add to destination collection
            val addResult = addGameToCollection(toCollectionId, gameId)
            if (addResult.isFailure) {
                // Try to rollback by adding back to source collection
                addGameToCollection(fromCollectionId, gameId)
                return addResult
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun addGamesToCollection(collectionId: String, gameIds: List<Int>): Result<List<Int>> {
        return try {
            val successfullyAdded = mutableListOf<Int>()
            
            for (gameId in gameIds) {
                val result = addGameToCollection(collectionId, gameId)
                if (result.isSuccess) {
                    successfullyAdded.add(gameId)
                }
            }
            
            Result.success(successfullyAdded)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun removeGamesFromCollection(collectionId: String, gameIds: List<Int>): Result<List<Int>> {
        return try {
            val successfullyRemoved = mutableListOf<Int>()
            
            for (gameId in gameIds) {
                val result = removeGameFromCollection(collectionId, gameId)
                if (result.isSuccess) {
                    successfullyRemoved.add(gameId)
                }
            }
            
            Result.success(successfullyRemoved)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun collectionNameExists(name: String, excludeId: String?): Result<Boolean> {
        return try {
            val collections = appDatabase.appDatabaseQueries.getAllCollections().executeAsList()
            val nameExists = collections.any { collection ->
                collection.name.equals(name, ignoreCase = true) && collection.id != excludeId
            }
            Result.success(nameExists)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun getCollectionGameCounts(): Result<Map<String, Int>> {
        return try {
            cacheMutex.withLock {
                // Check cache validity
                val currentTime = System.currentTimeMillis()
                if (cachedGameCounts != null && (currentTime - gameCountsCacheTimestamp) < cacheValidityDuration) {
                    return@withLock Result.success(cachedGameCounts!!)
                }
                
                // Fetch from database
                val collectionStats = appDatabase.appDatabaseQueries.getCollectionStats().executeAsList()
                val gameCounts = collectionStats.associate { stat ->
                    stat.id to stat.totalGames.toInt()
                }
                
                // Update cache
                cachedGameCounts = gameCounts
                gameCountsCacheTimestamp = currentTime
                
                Result.success(gameCounts)
            }
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun initializeDefaultCollections(): Result<List<GameCollection>> {
        return try {
            val createdCollections = mutableListOf<GameCollection>()
            val defaultTypes = CollectionType.getDefaultTypes()
            
            for (type in defaultTypes) {
                // Check if default collection already exists
                val existingCollections = getCollectionsByType(type)
                if (existingCollections.isSuccess && existingCollections.getOrThrow().isNotEmpty()) {
                    continue // Skip if already exists
                }
                
                // Create default collection
                val result = createCollection(type.displayName, type, type.description)
                if (result.isSuccess) {
                    createdCollections.add(result.getOrThrow())
                }
            }
            
            Result.success(createdCollections)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun clearCollection(collectionId: String): Result<Unit> {
        return try {
            // Check if collection exists
            val collection = appDatabase.appDatabaseQueries.getCollectionById(collectionId).executeAsOneOrNull()
            if (collection == null) {
                return Result.failure(CollectionError.CollectionNotFound(collectionId))
            }
            
            // Remove all games from collection
            appDatabase.appDatabaseQueries.removeAllGamesFromCollection(collectionId)
            
            // Update collection timestamp
            val currentTime = System.currentTimeMillis()
            appDatabase.appDatabaseQueries.updateCollection(
                collection.name,
                collection.description,
                currentTime,
                collectionId
            )
            
            // Clear cache
            clearCache()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    override suspend fun getCollectionsSorted(
        sortByType: Boolean,
        sortByName: Boolean,
        sortByGameCount: Boolean,
        ascending: Boolean
    ): Result<List<GameCollection>> {
        return try {
            val collectionsResult = getAllCollections()
            if (collectionsResult.isFailure) {
                return collectionsResult
            }
            
            var collections = collectionsResult.getOrThrow()
            
            // Apply sorting
            collections = when {
                sortByGameCount -> {
                    if (ascending) {
                        collections.sortedBy { it.gameIds.size }
                    } else {
                        collections.sortedByDescending { it.gameIds.size }
                    }
                }
                sortByType && sortByName -> {
                    collections.sortedWith(
                        compareBy<GameCollection> { it.type.sortOrder }
                            .thenBy { it.name.lowercase() }
                    )
                }
                sortByType -> {
                    collections.sortedBy { it.type.sortOrder }
                }
                sortByName -> {
                    if (ascending) {
                        collections.sortedBy { it.name.lowercase() }
                    } else {
                        collections.sortedByDescending { it.name.lowercase() }
                    }
                }
                else -> collections
            }
            
            Result.success(collections)
        } catch (e: Exception) {
            Result.failure(CollectionError.DatabaseError)
        }
    }
    
    // Helper methods
    private fun getGameIdsInCollection(collectionId: String): List<Int> {
        return try {
            val games = appDatabase.appDatabaseQueries.getGamesInCollection(collectionId).executeAsList()
            games.map { it.id.toInt() }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun clearCache() {
        cacheMutex.tryLock()
        try {
            cachedCollections = null
            cachedGameCounts = null
            cacheTimestamp = 0L
            gameCountsCacheTimestamp = 0L
        } finally {
            cacheMutex.unlock()
        }
    }
}