package com.devpush.features.game.domain.usecase

import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.model.collections.ValidationResult
import com.devpush.features.game.domain.repository.GameCollectionRepository

/**
 * Use case for updating collection properties with validation
 */
interface UpdateCollectionUseCase {
    /**
     * Updates a collection's name and/or description
     * @param collectionId The ID of the collection to update
     * @param newName The new name for the collection (null to keep current name)
     * @param newDescription The new description for the collection (null to keep current description)
     * @param allowDefaultUpdate Whether to allow updating default collections (usually false)
     * @return Result containing the updated GameCollection or error
     */
    suspend operator fun invoke(
        collectionId: String,
        newName: String? = null,
        newDescription: String? = null,
        allowDefaultUpdate: Boolean = false
    ): Result<GameCollection>
    
    /**
     * Updates a collection using a complete GameCollection object
     * @param updatedCollection The updated collection data
     * @param allowDefaultUpdate Whether to allow updating default collections
     * @return Result containing the updated GameCollection or error
     */
    suspend fun updateCollection(
        updatedCollection: GameCollection,
        allowDefaultUpdate: Boolean = false
    ): Result<GameCollection>
    
    /**
     * Validates if a collection can be updated
     * @param collectionId The ID of the collection to check
     * @param newName The proposed new name (null if not changing)
     * @param newDescription The proposed new description (null if not changing)
     * @return Result containing validation info or error
     */
    suspend fun validateUpdate(
        collectionId: String,
        newName: String? = null,
        newDescription: String? = null
    ): Result<UpdateValidationInfo>
}

/**
 * Data class containing validation information for collection updates
 */
data class UpdateValidationInfo(
    val canUpdate: Boolean,
    val isDefaultCollection: Boolean,
    val nameChanged: Boolean,
    val descriptionChanged: Boolean,
    val validationErrors: List<String>,
    val warnings: List<String>
)

class UpdateCollectionUseCaseImpl(
    private val repository: GameCollectionRepository
) : UpdateCollectionUseCase {
    
    override suspend fun invoke(
        collectionId: String,
        newName: String?,
        newDescription: String?,
        allowDefaultUpdate: Boolean
    ): Result<GameCollection> {
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
            
            // Get the current collection
            val currentCollectionResult = repository.getCollectionById(collectionId)
            val currentCollection = currentCollectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Check if this is a default collection and if updates are allowed
            if (currentCollection.type.isDefault && !allowDefaultUpdate) {
                return Result.failure(
                    CollectionError.DefaultCollectionProtected(
                        collectionType = currentCollection.type,
                        operation = "update"
                    )
                )
            }
            
            // Determine what's actually changing
            val finalName = newName?.trim() ?: currentCollection.name
            val finalDescription = newDescription?.trim() ?: currentCollection.description
            
            // If nothing is changing, return the current collection
            if (finalName == currentCollection.name && finalDescription == currentCollection.description) {
                return Result.success(currentCollection)
            }
            
            // Validate the update
            val validationResult = validateUpdate(collectionId, newName, newDescription)
            val validationInfo = validationResult.getOrElse { error ->
                return Result.failure(error)
            }
            
            if (!validationInfo.canUpdate) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection update",
                        reason = validationInfo.validationErrors.joinToString("; ")
                    )
                )
            }
            
            // Create updated collection
            val updatedCollection = currentCollection.copy(
                name = finalName,
                description = finalDescription,
                updatedAt = System.currentTimeMillis()
            )
            
            // Validate the updated collection
            val collectionValidation = updatedCollection.validate()
            if (collectionValidation.isInvalid()) {
                return Result.failure(
                    CollectionError.ValidationError(
                        field = "collection",
                        reason = collectionValidation.getErrorMessage() ?: "Invalid collection data"
                    )
                )
            }
            
            // Update in repository
            repository.updateCollection(updatedCollection)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to update collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun updateCollection(
        updatedCollection: GameCollection,
        allowDefaultUpdate: Boolean
    ): Result<GameCollection> {
        return try {
            // Get the current collection to compare
            val currentCollectionResult = repository.getCollectionById(updatedCollection.id)
            val currentCollection = currentCollectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(updatedCollection.id)
                )
            }
            
            // Use the main invoke method
            invoke(
                collectionId = updatedCollection.id,
                newName = if (updatedCollection.name != currentCollection.name) updatedCollection.name else null,
                newDescription = if (updatedCollection.description != currentCollection.description) updatedCollection.description else null,
                allowDefaultUpdate = allowDefaultUpdate
            )
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to update collection: ${e.message}", e)
            )
        }
    }
    
    override suspend fun validateUpdate(
        collectionId: String,
        newName: String?,
        newDescription: String?
    ): Result<UpdateValidationInfo> {
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
            
            // Get the current collection
            val currentCollectionResult = repository.getCollectionById(collectionId)
            val currentCollection = currentCollectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            val validationErrors = mutableListOf<String>()
            val warnings = mutableListOf<String>()
            
            // Determine what's changing
            val finalName = newName?.trim() ?: currentCollection.name
            val finalDescription = newDescription?.trim() ?: currentCollection.description
            val nameChanged = finalName != currentCollection.name
            val descriptionChanged = finalDescription != currentCollection.description
            val isDefaultCollection = currentCollection.type.isDefault
            
            // Validate new name if it's changing
            if (nameChanged) {
                // Create a temporary collection to validate the name
                val tempCollection = currentCollection.copy(name = finalName)
                val nameValidation = tempCollection.validateName()
                
                if (nameValidation.isInvalid()) {
                    validationErrors.add(nameValidation.getErrorMessage() ?: "Invalid name")
                }
                
                // Check for duplicate name (excluding current collection)
                val nameExistsResult = repository.collectionNameExists(finalName, collectionId)
                nameExistsResult.fold(
                    onSuccess = { exists ->
                        if (exists) {
                            validationErrors.add("Collection name '$finalName' already exists")
                        }
                    },
                    onFailure = { error ->
                        return Result.failure(
                            if (error is CollectionError) error
                            else CollectionError.DatabaseError
                        )
                    }
                )
                
                // Check if trying to rename to a default collection name
                if (currentCollection.type != CollectionType.CUSTOM) {
                    val defaultType = CollectionType.findByDisplayName(finalName)
                    if (defaultType != null && defaultType != currentCollection.type) {
                        validationErrors.add("Cannot rename to another default collection name")
                    }
                }
                
                // Warn about renaming default collections
                if (isDefaultCollection) {
                    warnings.add("Renaming default collections may affect user experience")
                }
            }
            
            // Validate new description if it's changing
            if (descriptionChanged) {
                val tempCollection = currentCollection.copy(description = finalDescription)
                val descriptionValidation = tempCollection.validateDescription()
                
                if (descriptionValidation.isInvalid()) {
                    validationErrors.add(descriptionValidation.getErrorMessage() ?: "Invalid description")
                }
            }
            
            val canUpdate = validationErrors.isEmpty()
            
            val validationInfo = UpdateValidationInfo(
                canUpdate = canUpdate,
                isDefaultCollection = isDefaultCollection,
                nameChanged = nameChanged,
                descriptionChanged = descriptionChanged,
                validationErrors = validationErrors,
                warnings = warnings
            )
            
            Result.success(validationInfo)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to validate update: ${e.message}", e)
            )
        }
    }
}