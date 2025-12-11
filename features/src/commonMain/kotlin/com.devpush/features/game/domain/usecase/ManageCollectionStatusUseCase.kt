package com.devpush.features.game.domain.usecase

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.collections.domain.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Data class representing a status transition confirmation
 */
data class StatusTransitionConfirmation(
    val gameId: Int,
    val fromCollectionId: String,
    val toCollectionId: String,
    val fromType: CollectionType,
    val toType: CollectionType,
    val message: String,
    val requiresConfirmation: Boolean
)

/**
 * Use case for managing game status transitions between collections
 * Handles automatic moves between status collections (Wishlist -> Currently Playing -> Completed)
 * with user confirmation when needed
 */
interface ManageCollectionStatusUseCase {
    /**
     * Moves a game between status collections with appropriate confirmation logic
     * @param gameId The ID of the game to move
     * @param fromCollectionId The ID of the source collection
     * @param toCollectionId The ID of the destination collection
     * @param skipConfirmation Whether to skip confirmation prompts (for programmatic moves)
     * @return Result indicating success or error, or confirmation needed
     */
    suspend fun moveGameBetweenStatusCollections(
        gameId: Int,
        fromCollectionId: String,
        toCollectionId: String,
        skipConfirmation: Boolean = false
    ): Result<Unit>
    
    /**
     * Gets confirmation details for a potential status transition
     * @param gameId The ID of the game
     * @param fromCollectionId The ID of the source collection
     * @param toCollectionId The ID of the destination collection
     * @return Result containing StatusTransitionConfirmation or error
     */
    suspend fun getStatusTransitionConfirmation(
        gameId: Int,
        fromCollectionId: String,
        toCollectionId: String
    ): Result<StatusTransitionConfirmation>
    
    /**
     * Automatically moves a game to the next logical status collection
     * @param gameId The ID of the game
     * @param currentCollectionId The ID of the current collection
     * @return Result containing the destination collection ID or error
     */
    suspend fun moveToNextStatus(
        gameId: Int,
        currentCollectionId: String
    ): Result<String>
    
    /**
     * Gets all collections containing a specific game
     * @param gameId The ID of the game
     * @return Result containing list of collections that contain the game
     */
    suspend fun getGameCollections(gameId: Int): Result<List<GameCollection>>
}

class ManageCollectionStatusUseCaseImpl(
    private val repository: GameCollectionRepository
) : ManageCollectionStatusUseCase {
    
    override suspend fun moveGameBetweenStatusCollections(
        gameId: Int,
        fromCollectionId: String,
        toCollectionId: String,
        skipConfirmation: Boolean
    ): Result<Unit> {
        return try {
            // Get confirmation details first
            if (!skipConfirmation) {
                val confirmationResult = getStatusTransitionConfirmation(gameId, fromCollectionId, toCollectionId)
                if (confirmationResult.isFailure) {
                    return Result.failure(confirmationResult.exceptionOrNull() ?: CollectionError.UnknownError("Failed to get confirmation details"))
                }
                
                val confirmation = confirmationResult.getOrThrow()
                if (confirmation.requiresConfirmation) {
                    // Return a special error that indicates confirmation is needed
                    return Result.failure(
                        CollectionError.ConfirmationRequired(
                            message = confirmation.message,
                            gameId = gameId,
                            fromCollectionId = fromCollectionId,
                            toCollectionId = toCollectionId
                        )
                    )
                }
            }
            
            // Perform the move
            repository.moveGameBetweenCollections(gameId, fromCollectionId, toCollectionId)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to move game between collections: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getStatusTransitionConfirmation(
        gameId: Int,
        fromCollectionId: String,
        toCollectionId: String
    ): Result<StatusTransitionConfirmation> {
        return try {
            // Get both collections
            val fromCollectionResult = repository.getCollectionById(fromCollectionId)
            val toCollectionResult = repository.getCollectionById(toCollectionId)
            
            if (fromCollectionResult.isFailure) {
                return Result.failure(fromCollectionResult.exceptionOrNull() ?: CollectionError.CollectionNotFound(fromCollectionId))
            }
            
            if (toCollectionResult.isFailure) {
                return Result.failure(toCollectionResult.exceptionOrNull() ?: CollectionError.CollectionNotFound(toCollectionId))
            }
            
            val fromCollection = fromCollectionResult.getOrThrow()
            val toCollection = toCollectionResult.getOrThrow()
            
            // Check if confirmation is required
            val requiresConfirmation = fromCollection.type.requiresConfirmationToMoveTo(toCollection.type)
            val message = fromCollection.type.getMoveMessage(toCollection.type) 
                ?: "Move game from ${fromCollection.name} to ${toCollection.name}?"
            
            val confirmation = StatusTransitionConfirmation(
                gameId = gameId,
                fromCollectionId = fromCollectionId,
                toCollectionId = toCollectionId,
                fromType = fromCollection.type,
                toType = toCollection.type,
                message = message,
                requiresConfirmation = requiresConfirmation
            )
            
            Result.success(confirmation)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get status transition confirmation: ${e.message}", e)
            )
        }
    }
    
    override suspend fun moveToNextStatus(
        gameId: Int,
        currentCollectionId: String
    ): Result<String> {
        return try {
            // Get current collection
            val currentCollectionResult = repository.getCollectionById(currentCollectionId)
            if (currentCollectionResult.isFailure) {
                return Result.failure(currentCollectionResult.exceptionOrNull() ?: CollectionError.CollectionNotFound(currentCollectionId))
            }
            
            val currentCollection = currentCollectionResult.getOrThrow()
            val nextStatus = currentCollection.type.getNextStatus()
            
            if (nextStatus == null) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection type",
                        reason = "No next status available for ${currentCollection.type.displayName}"
                    )
                )
            }
            
            // Find collection of the next status type
            val nextCollectionsResult = repository.getCollectionsByType(nextStatus)
            if (nextCollectionsResult.isFailure) {
                return Result.failure(nextCollectionsResult.exceptionOrNull() ?: CollectionError.DatabaseError)
            }
            
            val nextCollections = nextCollectionsResult.getOrThrow()
            if (nextCollections.isEmpty()) {
                return Result.failure(
                    CollectionError.CollectionNotFound("No ${nextStatus.displayName} collection found")
                )
            }
            
            // Use the first collection of the target type (should be the default one)
            val targetCollection = nextCollections.first()
            
            // Move the game
            val moveResult = moveGameBetweenStatusCollections(
                gameId = gameId,
                fromCollectionId = currentCollectionId,
                toCollectionId = targetCollection.id,
                skipConfirmation = false // Let the user confirm status transitions
            )
            
            if (moveResult.isFailure) {
                return Result.failure(moveResult.exceptionOrNull() ?: CollectionError.UnknownError("Failed to move to next status"))
            }
            
            Result.success(targetCollection.id)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to move to next status: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getGameCollections(gameId: Int): Result<List<GameCollection>> {
        return try {
            // Get collection IDs containing the game
            val collectionIdsResult = repository.getCollectionsContainingGame(gameId)
            if (collectionIdsResult.isFailure) {
                return Result.failure(collectionIdsResult.exceptionOrNull() ?: CollectionError.DatabaseError)
            }
            
            val collectionIds = collectionIdsResult.getOrThrow()
            val collections = mutableListOf<GameCollection>()
            
            // Get each collection
            for (collectionId in collectionIds) {
                val collectionResult = repository.getCollectionById(collectionId)
                if (collectionResult.isSuccess) {
                    collections.add(collectionResult.getOrThrow())
                }
            }
            
            Result.success(collections)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get game collections: ${e.message}", e)
            )
        }
    }
}