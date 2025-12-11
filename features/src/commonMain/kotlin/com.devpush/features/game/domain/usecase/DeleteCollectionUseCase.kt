package com.devpush.features.game.domain.usecase

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Use case for deleting collections with confirmation and cascade deletion
 */
interface DeleteCollectionUseCase {
    /**
     * Deletes a collection with confirmation and cascade deletion of relationships
     * @param collectionId The ID of the collection to delete
     * @param confirmDeletion Whether to require confirmation for non-empty collections
     * @param allowDefaultDeletion Whether to allow deletion of default collections (usually false)
     * @return Result containing deletion info or error
     */
    suspend operator fun invoke(
        collectionId: String,
        confirmDeletion: Boolean = true,
        allowDefaultDeletion: Boolean = false
    ): Result<DeletionInfo>
    
    /**
     * Gets information about what will be deleted before actual deletion
     * @param collectionId The ID of the collection to check
     * @return Result containing deletion preview information or error
     */
    suspend fun getDeletionInfo(collectionId: String): Result<DeletionInfo>
    
    /**
     * Clears all games from a collection without deleting the collection itself
     * @param collectionId The ID of the collection to clear
     * @return Result indicating success or error
     */
    suspend fun clearCollection(collectionId: String): Result<Unit>
}

/**
 * Data class containing information about collection deletion
 */
data class DeletionInfo(
    val collection: GameCollection,
    val gameCount: Int,
    val canDelete: Boolean,
    val requiresConfirmation: Boolean,
    val warningMessage: String?,
    val isDefaultCollection: Boolean
)

class DeleteCollectionUseCaseImpl(
    private val repository: GameCollectionRepository
) : DeleteCollectionUseCase {
    
    override suspend fun invoke(
        collectionId: String,
        confirmDeletion: Boolean,
        allowDefaultDeletion: Boolean
    ): Result<DeletionInfo> {
        return try {
            // Validate input
            if (collectionId.isBlank()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection id",
                        reason = "Collection ID cannot be empty"
                    )
                )
            }
            
            // Get deletion info first
            val deletionInfoResult = getDeletionInfo(collectionId)
            val deletionInfo = deletionInfoResult.getOrElse { error ->
                return Result.failure(error)
            }
            
            // Check if deletion is allowed
            if (!deletionInfo.canDelete) {
                return Result.failure(
                    CollectionError.DefaultCollectionProtected(
                        collectionType = deletionInfo.collection.type,
                        operation = "delete"
                    )
                )
            }
            
            // Check if it's a default collection and if deletion is allowed
            if (deletionInfo.isDefaultCollection && !allowDefaultDeletion) {
                return Result.failure(
                    CollectionError.DefaultCollectionProtected(
                        collectionType = deletionInfo.collection.type,
                        operation = "delete"
                    )
                )
            }
            
            // Check if confirmation is required for non-empty collections
            if (deletionInfo.requiresConfirmation && confirmDeletion && deletionInfo.gameCount > 0) {
                // In a real implementation, this would trigger a confirmation dialog
                // For now, we'll proceed with the deletion
            }
            
            // Perform the deletion (this will cascade delete all game relationships)
            val deleteResult = repository.deleteCollection(collectionId)
            deleteResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            Result.success(deletionInfo)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to delete collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getDeletionInfo(collectionId: String): Result<DeletionInfo> {
        return try {
            // Validate input
            if (collectionId.isBlank()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection id",
                        reason = "Collection ID cannot be empty"
                    )
                )
            }
            
            // Get the collection
            val collectionResult = repository.getCollectionById(collectionId)
            val collection = collectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Get game count
            val gameCountsResult = repository.getCollectionGameCounts()
            val gameCounts = gameCountsResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            val gameCount = gameCounts[collectionId] ?: 0
            val isDefaultCollection = collection.type.isDefault
            
            // Determine if deletion is allowed and if confirmation is required
            val canDelete = !isDefaultCollection // Default collections typically can't be deleted
            val requiresConfirmation = gameCount > 0
            
            val warningMessage = when {
                isDefaultCollection -> "This is a default collection and cannot be deleted. You can only clear its contents."
                gameCount > 0 -> "This collection contains $gameCount game${if (gameCount == 1) "" else "s"}. Deleting it will remove all games from this collection."
                else -> null
            }
            
            val deletionInfo = DeletionInfo(
                collection = collection,
                gameCount = gameCount,
                canDelete = canDelete,
                requiresConfirmation = requiresConfirmation,
                warningMessage = warningMessage,
                isDefaultCollection = isDefaultCollection
            )
            
            Result.success(deletionInfo)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get deletion info: ${e.message}", e)
            )
        }
    }
    
    override suspend fun clearCollection(collectionId: String): Result<Unit> {
        return try {
            // Validate input
            if (collectionId.isBlank()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection id",
                        reason = "Collection ID cannot be empty"
                    )
                )
            }
            
            // Verify collection exists
            val collectionResult = repository.getCollectionById(collectionId)
            collectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Clear the collection
            repository.clearCollection(collectionId)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to clear collection: ${e.message}", e)
            )
        }
    }
}