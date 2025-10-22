package com.devpush.features.game.domain.usecase

import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.bookmarklist.domain.collections.CollectionError
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Use case for initializing default collections on app start
 * Ensures that the three main collection types (Wishlist, Currently Playing, Completed) 
 * are always available to users
 */
interface InitializeDefaultCollectionsUseCase {
    /**
     * Initializes all default collections if they don't exist
     * This should be called on app startup to ensure default collections are available
     * @return Result containing list of created default collections or error
     */
    suspend operator fun invoke(): Result<List<GameCollection>>
    
    /**
     * Checks if all default collections exist
     * @return Result containing true if all default collections exist, false otherwise
     */
    suspend fun areDefaultCollectionsInitialized(): Result<Boolean>
}

class InitializeDefaultCollectionsUseCaseImpl(
    private val repository: GameCollectionRepository
) : InitializeDefaultCollectionsUseCase {
    
    override suspend fun invoke(): Result<List<GameCollection>> {
        return try {
            // Use the repository's initialization method
            val result = repository.initializeDefaultCollections()
            
            result.fold(
                onSuccess = { createdCollections ->
                    // Log successful initialization if needed
                    Result.success(createdCollections)
                },
                onFailure = { error ->
                    // Handle specific errors
                    when (error) {
                        is CollectionError.DatabaseError -> {
                            Result.failure(
                                CollectionError.UnknownError(
                                    "Failed to initialize default collections due to database error",
                                    error
                                )
                            )
                        }
                        is CollectionError -> Result.failure(error)
                        else -> Result.failure(
                            CollectionError.UnknownError(
                                "Failed to initialize default collections: ${error.message}",
                                error
                            )
                        )
                    }
                }
            )
        } catch (e: Exception) {
            Result.failure(
                CollectionError.UnknownError(
                    "Unexpected error during default collections initialization: ${e.message}",
                    e
                )
            )
        }
    }
    
    override suspend fun areDefaultCollectionsInitialized(): Result<Boolean> {
        return try {
            // Get all collections and check if default types exist
            val collectionsResult = repository.getAllCollections()
            
            collectionsResult.fold(
                onSuccess = { collections ->
                    val defaultTypes = CollectionType.getDefaultTypes()
                    val existingTypes = collections.map { it.type }.toSet()
                    
                    // Check if all default types are present
                    val allDefaultsExist = defaultTypes.all { defaultType ->
                        existingTypes.contains(defaultType)
                    }
                    
                    Result.success(allDefaultsExist)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(
                CollectionError.UnknownError(
                    "Failed to check default collections status: ${e.message}",
                    e
                )
            )
        }
    }
}