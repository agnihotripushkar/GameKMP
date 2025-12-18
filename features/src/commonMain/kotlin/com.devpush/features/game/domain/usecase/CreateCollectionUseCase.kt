package com.devpush.features.game.domain.usecase

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.collections.domain.collections.CollectionError
import com.devpush.features.collections.domain.collections.ValidationResult
import com.devpush.features.game.domain.repository.GameCollectionRepository
import kotlinx.datetime.Clock


/**
 * Use case for creating new game collections with validation and default collection initialization
 */
interface CreateCollectionUseCase {
    /**
     * Creates a new collection with validation
     * @param name The name of the collection
     * @param type The type of collection (predefined or custom)
     * @param description Optional description for the collection
     * @return Result containing the created GameCollection or error
     */
    suspend operator fun invoke(
        name: String,
        type: CollectionType,
        description: String? = null
    ): Result<GameCollection>
    
    /**
     * Initializes all default collections if they don't exist
     * @return Result containing list of created default collections or error
     */
    suspend fun initializeDefaultCollections(): Result<List<GameCollection>>
}

class CreateCollectionUseCaseImpl(
    private val repository: GameCollectionRepository
) : CreateCollectionUseCase {
    
    override suspend fun invoke(
        name: String,
        type: CollectionType,
        description: String?
    ): Result<GameCollection> {
        return try {
            // Validate input parameters
            val validationResult = validateInput(name, type, description)
            if (validationResult.isInvalid()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection name",
                        reason = validationResult.getErrorMessage() ?: "Invalid input"
                    )
                )
            }
            
            // Check for duplicate name
            val nameExistsResult = repository.collectionNameExists(name.trim())
            nameExistsResult.fold(
                onSuccess = { exists ->
                    if (exists) {
                        return Result.failure(CollectionError.CollectionNameExists(name.trim()))
                    }
                },
                onFailure = { error ->
                    return Result.failure(
                        if (error is CollectionError) error 
                        else CollectionError.DatabaseError
                    )
                }
            )
            
            // Validate type and name combination for default collections
            if (!CollectionType.isValidTypeForName(type, name.trim())) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection type",
                        reason = "Collection name '${name.trim()}' is reserved for default collections"
                    )
                )
            }
            
            // Create the collection
            repository.createCollection(name.trim(), type, description?.trim())
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to create collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun initializeDefaultCollections(): Result<List<GameCollection>> {
        return try {
            // Get all default collection types
            val defaultTypes = CollectionType.getDefaultTypes()
            val createdCollections = mutableListOf<GameCollection>()
            
            // Check and create each default collection if it doesn't exist
            for (defaultType in defaultTypes) {
                val existingCollectionsResult = repository.getCollectionsByType(defaultType)
                
                existingCollectionsResult.fold(
                    onSuccess = { existingCollections ->
                        // If no collection of this type exists, create it
                        if (existingCollections.isEmpty()) {
                            val createResult = repository.createCollection(
                                name = defaultType.displayName,
                                type = defaultType,
                                description = defaultType.description
                            )
                            
                            createResult.fold(
                                onSuccess = { collection ->
                                    createdCollections.add(collection)
                                },
                                onFailure = { error ->
                                    // If it's a duplicate name error, it means the collection already exists
                                    // which is fine for initialization
                                    if (error !is CollectionError.CollectionNameExists) {
                                        return Result.failure(error)
                                    }
                                }
                            )
                        }
                    },
                    onFailure = { error ->
                        return Result.failure(
                            if (error is CollectionError) error
                            else CollectionError.DatabaseError
                        )
                    }
                )
            }
            
            Result.success(createdCollections)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to initialize default collections: ${e.message}", e)
            )
        }
    }
    
    /**
     * Validates the input parameters for collection creation
     */
    private fun validateInput(
        name: String,
        type: CollectionType,
        description: String?
    ): ValidationResult {
        // Create a temporary collection to use its validation methods
        val tempCollection = GameCollection(
            id = "temp",
            name = name,
            type = type,
            gameIds = emptyList(),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds(),
            description = description

        )
        
        // Validate name
        val nameValidation = tempCollection.validateName()
        if (nameValidation.isInvalid()) {
            return nameValidation
        }
        
        // Validate description
        val descriptionValidation = tempCollection.validateDescription()
        if (descriptionValidation.isInvalid()) {
            return descriptionValidation
        }
        
        return ValidationResult.Valid
    }
}