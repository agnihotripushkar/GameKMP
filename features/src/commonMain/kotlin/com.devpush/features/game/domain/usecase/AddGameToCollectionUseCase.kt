package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Use case for adding games to collections with duplicate prevention and status transitions
 */
interface AddGameToCollectionUseCase {
    /**
     * Adds a game to a collection with duplicate prevention
     * @param collectionId The ID of the collection to add the game to
     * @param gameId The ID of the game to add
     * @param confirmTransition Whether to confirm status transitions between collection types
     * @return Result containing success or error information
     */
    suspend operator fun invoke(
        collectionId: String,
        gameId: Int,
        confirmTransition: Boolean = true
    ): Result<Unit>
    
    /**
     * Adds a game to a collection by type (useful for default collections)
     * @param gameId The ID of the game to add
     * @param collectionType The type of collection to add the game to
     * @param confirmTransition Whether to confirm status transitions between collection types
     * @return Result containing success or error information
     */
    suspend fun addToCollectionByType(
        gameId: Int,
        collectionType: CollectionType,
        confirmTransition: Boolean = true
    ): Result<Unit>
    
    /**
     * Gets information about potential status transitions when adding a game
     * @param gameId The ID of the game
     * @param targetCollectionId The ID of the target collection
     * @return Result containing transition information or error
     */
    suspend fun getTransitionInfo(
        gameId: Int,
        targetCollectionId: String
    ): Result<TransitionInfo>
}

/**
 * Data class containing information about collection transitions
 */
data class TransitionInfo(
    val requiresConfirmation: Boolean,
    val currentCollections: List<GameCollection>,
    val targetCollection: GameCollection,
    val transitionMessage: String?,
    val conflictingCollections: List<GameCollection>
)

class AddGameToCollectionUseCaseImpl(
    private val repository: GameCollectionRepository
) : AddGameToCollectionUseCase {
    
    override suspend fun invoke(
        collectionId: String,
        gameId: Int,
        confirmTransition: Boolean
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
            
            // Get target collection
            val targetCollectionResult = repository.getCollectionById(collectionId)
            val targetCollection = targetCollectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Check if game is already in this collection
            if (targetCollection.containsGame(gameId)) {
                return Result.failure(
                    CollectionError.DuplicateGameInCollection(gameId, targetCollection.name)
                )
            }
            
            // Get transition information
            val transitionInfoResult = getTransitionInfo(gameId, collectionId)
            val transitionInfo = transitionInfoResult.getOrElse { error ->
                return Result.failure(error)
            }
            
            // Handle status transitions if confirmation is required
            if (transitionInfo.requiresConfirmation && confirmTransition) {
                // For status collections, handle automatic transitions
                if (targetCollection.type.isStatusCollection()) {
                    val handleTransitionResult = handleStatusTransition(
                        gameId = gameId,
                        targetCollection = targetCollection,
                        currentCollections = transitionInfo.currentCollections
                    )
                    
                    handleTransitionResult.getOrElse { error ->
                        return Result.failure(error)
                    }
                }
            }
            
            // Add the game to the target collection
            repository.addGameToCollection(collectionId, gameId)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to add game to collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun addToCollectionByType(
        gameId: Int,
        collectionType: CollectionType,
        confirmTransition: Boolean
    ): Result<Unit> {
        return try {
            // Get collections of the specified type
            val collectionsResult = repository.getCollectionsByType(collectionType)
            val collections = collectionsResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            // For default types, there should be exactly one collection
            val targetCollection = if (collectionType.isDefault) {
                collections.firstOrNull() ?: run {
                    // If default collection doesn't exist, create it first
                    val createResult = repository.createCollection(
                        name = collectionType.displayName,
                        type = collectionType,
                        description = collectionType.description
                    )
                    createResult.getOrElse { error ->
                        return Result.failure(error)
                    }
                }
            } else {
                // For custom collections, this method doesn't make sense
                return Result.failure(
                    CollectionError.InvalidCollectionOperation(
                        operation = "add by type",
                        reason = "Cannot add to custom collection type without specifying collection ID"
                    )
                )
            }
            
            // Use the main invoke method
            invoke(targetCollection.id, gameId, confirmTransition)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to add game to collection by type: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getTransitionInfo(
        gameId: Int,
        targetCollectionId: String
    ): Result<TransitionInfo> {
        return try {
            // Get target collection
            val targetCollectionResult = repository.getCollectionById(targetCollectionId)
            val targetCollection = targetCollectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(targetCollectionId)
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
            
            // Get the actual collection objects
            val currentCollections = mutableListOf<GameCollection>()
            for (collectionId in collectionIds) {
                val collectionResult = repository.getCollectionById(collectionId)
                collectionResult.fold(
                    onSuccess = { collection ->
                        currentCollections.add(collection)
                    },
                    onFailure = { 
                        // Skip collections that can't be found (they might have been deleted)
                    }
                )
            }
            
            // Determine if confirmation is required and find conflicting collections
            val conflictingCollections = mutableListOf<GameCollection>()
            var requiresConfirmation = false
            var transitionMessage: String? = null
            
            if (targetCollection.type.isStatusCollection()) {
                // Check for conflicts with other status collections
                val statusCollections = currentCollections.filter { it.type.isStatusCollection() }
                
                for (statusCollection in statusCollections) {
                    if (statusCollection.type.requiresConfirmationToMoveTo(targetCollection.type)) {
                        requiresConfirmation = true
                        conflictingCollections.add(statusCollection)
                        transitionMessage = statusCollection.type.getMoveMessage(targetCollection.type)
                    }
                }
            }
            
            val transitionInfo = TransitionInfo(
                requiresConfirmation = requiresConfirmation,
                currentCollections = currentCollections,
                targetCollection = targetCollection,
                transitionMessage = transitionMessage,
                conflictingCollections = conflictingCollections
            )
            
            Result.success(transitionInfo)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get transition info: ${e.message}", e)
            )
        }
    }
    
    /**
     * Handles status transitions between collection types
     */
    private suspend fun handleStatusTransition(
        gameId: Int,
        targetCollection: GameCollection,
        currentCollections: List<GameCollection>
    ): Result<Unit> {
        return try {
            val statusCollections = currentCollections.filter { it.type.isStatusCollection() }
            
            for (statusCollection in statusCollections) {
                when {
                    // Moving from Wishlist to Currently Playing - remove from wishlist
                    statusCollection.type == CollectionType.WISHLIST && 
                    targetCollection.type == CollectionType.CURRENTLY_PLAYING -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                    
                    // Moving from Currently Playing to Completed - remove from currently playing
                    statusCollection.type == CollectionType.CURRENTLY_PLAYING && 
                    targetCollection.type == CollectionType.COMPLETED -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                    
                    // Moving from Wishlist to Completed - remove from wishlist (skip currently playing)
                    statusCollection.type == CollectionType.WISHLIST && 
                    targetCollection.type == CollectionType.COMPLETED -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                    
                    // Moving back from Completed to Currently Playing - remove from completed
                    statusCollection.type == CollectionType.COMPLETED && 
                    targetCollection.type == CollectionType.CURRENTLY_PLAYING -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                    
                    // Moving back from Completed to Wishlist - remove from completed
                    statusCollection.type == CollectionType.COMPLETED && 
                    targetCollection.type == CollectionType.WISHLIST -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                    
                    // Moving back from Currently Playing to Wishlist - remove from currently playing
                    statusCollection.type == CollectionType.CURRENTLY_PLAYING && 
                    targetCollection.type == CollectionType.WISHLIST -> {
                        repository.removeGameFromCollection(statusCollection.id, gameId)
                    }
                }
            }
            
            Result.success(Unit)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to handle status transition: ${e.message}", e)
            )
        }
    }
}