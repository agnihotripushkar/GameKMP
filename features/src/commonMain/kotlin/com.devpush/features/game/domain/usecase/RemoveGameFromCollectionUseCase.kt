package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Use case for removing games from collections with immediate updates
 */
interface RemoveGameFromCollectionUseCase {
    /**
     * Removes a game from a collection
     * @param collectionId The ID of the collection to remove the game from
     * @param gameId The ID of the game to remove
     * @return Result indicating success or error
     */
    suspend operator fun invoke(
        collectionId: String,
        gameId: Int
    ): Result<Unit>
    
    /**
     * Removes a game from multiple collections
     * @param collectionIds List of collection IDs to remove the game from
     * @param gameId The ID of the game to remove
     * @return Result containing list of successfully processed collection IDs or error
     */
    suspend fun removeFromMultipleCollections(
        collectionIds: List<String>,
        gameId: Int
    ): Result<List<String>>
    
    /**
     * Removes a game from all collections it belongs to
     * @param gameId The ID of the game to remove from all collections
     * @return Result containing list of collection IDs the game was removed from or error
     */
    suspend fun removeFromAllCollections(gameId: Int): Result<List<String>>
}

class RemoveGameFromCollectionUseCaseImpl(
    private val repository: GameCollectionRepository
) : RemoveGameFromCollectionUseCase {
    
    override suspend fun invoke(
        collectionId: String,
        gameId: Int
    ): Result<Unit> {
        return try {
            // Validate input
            if (gameId <= 0) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "game id",
                        reason = "Game ID must be positive"
                    )
                )
            }
            
            if (collectionId.isBlank()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection id",
                        reason = "Collection ID cannot be empty"
                    )
                )
            }
            
            // Get the collection to verify it exists and check if game is in it
            val collectionResult = repository.getCollectionById(collectionId)
            val collection = collectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Check if the game is actually in the collection
            if (!collection.containsGame(gameId)) {
                return Result.failure(
                    CollectionError.GameNotInCollection(gameId, collection.name)
                )
            }
            
            // Remove the game from the collection
            repository.removeGameFromCollection(collectionId, gameId)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to remove game from collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun removeFromMultipleCollections(
        collectionIds: List<String>,
        gameId: Int
    ): Result<List<String>> {
        return try {
            // Validate input
            if (gameId <= 0) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "game id",
                        reason = "Game ID must be positive"
                    )
                )
            }
            
            if (collectionIds.isEmpty()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection ids",
                        reason = "Collection IDs list cannot be empty"
                    )
                )
            }
            
            val successfullyProcessed = mutableListOf<String>()
            val errors = mutableListOf<CollectionError>()
            
            // Process each collection
            for (collectionId in collectionIds) {
                val removeResult = invoke(collectionId, gameId)
                removeResult.fold(
                    onSuccess = {
                        successfullyProcessed.add(collectionId)
                    },
                    onFailure = { error ->
                        // For batch operations, we collect errors but continue processing
                        if (error is CollectionError) {
                            errors.add(error)
                        } else {
                            errors.add(CollectionError.UnknownError("Failed to process collection $collectionId: ${error.message}"))
                        }
                    }
                )
            }
            
            // If no collections were processed successfully, return the first error
            if (successfullyProcessed.isEmpty() && errors.isNotEmpty()) {
                return Result.failure(errors.first())
            }
            
            Result.success(successfullyProcessed)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to remove game from multiple collections: ${e.message}", e)
            )
        }
    }
    
    override suspend fun removeFromAllCollections(gameId: Int): Result<List<String>> {
        return try {
            // Validate input
            if (gameId <= 0) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "game id",
                        reason = "Game ID must be positive"
                    )
                )
            }
            
            // Get all collections containing this game
            val collectionsContainingGameResult = repository.getCollectionsContainingGame(gameId)
            val collectionIds = collectionsContainingGameResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            // If the game is not in any collections, return empty list
            if (collectionIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Remove from all collections
            removeFromMultipleCollections(collectionIds, gameId)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to remove game from all collections: ${e.message}", e)
            )
        }
    }
}