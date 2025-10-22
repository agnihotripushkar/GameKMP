package com.devpush.features.bookmarklist.domain.collections

/**
 * Domain model representing a game collection with validation methods
 * 
 * @param id Unique identifier for the collection
 * @param name User-defined name for the collection
 * @param type Type of collection (predefined or custom)
 * @param gameIds List of game IDs in this collection
 * @param createdAt Timestamp when collection was created
 * @param updatedAt Timestamp when collection was last modified
 * @param description Optional description for the collection
 */
data class GameCollection(
    val id: String,
    val name: String,
    val type: CollectionType,
    val gameIds: List<Int> = emptyList(),
    val createdAt: Long,
    val updatedAt: Long,
    val description: String? = null
) {
    
    /**
     * Validates the collection name
     * @return ValidationResult indicating if the name is valid
     */
    fun validateName(): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult.Invalid("Collection name cannot be empty")
            name.length < 2 -> ValidationResult.Invalid("Collection name must be at least 2 characters")
            name.length > 50 -> ValidationResult.Invalid("Collection name cannot exceed 50 characters")
            name.trim() != name -> ValidationResult.Invalid("Collection name cannot have leading or trailing spaces")
            !name.matches(Regex("^[a-zA-Z0-9\\s\\-_'\".,!?()]+$")) -> ValidationResult.Invalid("Collection name contains invalid characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates the collection description if present
     * @return ValidationResult indicating if the description is valid
     */
    fun validateDescription(): ValidationResult {
        return when {
            description == null -> ValidationResult.Valid
            description.length > 200 -> ValidationResult.Invalid("Collection description cannot exceed 200 characters")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Validates the entire collection
     * @return ValidationResult indicating if the collection is valid
     */
    fun validate(): ValidationResult {
        val nameValidation = validateName()
        if (nameValidation is ValidationResult.Invalid) {
            return nameValidation
        }
        
        val descriptionValidation = validateDescription()
        if (descriptionValidation is ValidationResult.Invalid) {
            return descriptionValidation
        }
        
        return when {
            createdAt <= 0 -> ValidationResult.Invalid("Invalid creation timestamp")
            updatedAt <= 0 -> ValidationResult.Invalid("Invalid update timestamp")
            updatedAt < createdAt -> ValidationResult.Invalid("Update timestamp cannot be before creation timestamp")
            gameIds.any { it <= 0 } -> ValidationResult.Invalid("Invalid game ID found in collection")
            else -> ValidationResult.Valid
        }
    }
    
    /**
     * Checks if the collection contains a specific game
     * @param gameId The ID of the game to check
     * @return true if the game is in the collection, false otherwise
     */
    fun containsGame(gameId: Int): Boolean {
        return gameIds.contains(gameId)
    }
    
    /**
     * Gets the number of games in the collection
     * @return The count of games in this collection
     */
    fun getGameCount(): Int {
        return gameIds.size
    }
    
    /**
     * Checks if the collection is empty
     * @return true if the collection has no games, false otherwise
     */
    fun isEmpty(): Boolean {
        return gameIds.isEmpty()
    }
    
    /**
     * Checks if this is a default collection type
     * @return true if this is a predefined collection type, false for custom
     */
    fun isDefaultCollection(): Boolean {
        return type.isDefault
    }
    
    /**
     * Creates a copy of this collection with an updated timestamp
     * @param newUpdatedAt The new update timestamp, defaults to current time
     * @return A new GameCollection instance with updated timestamp
     */
    fun withUpdatedTimestamp(newUpdatedAt: Long = System.currentTimeMillis()): GameCollection {
        return copy(updatedAt = newUpdatedAt)
    }
    
    /**
     * Creates a copy of this collection with a new game added
     * @param gameId The ID of the game to add
     * @return A new GameCollection instance with the game added
     */
    fun withGameAdded(gameId: Int): GameCollection {
        return if (containsGame(gameId)) {
            this
        } else {
            copy(
                gameIds = gameIds + gameId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Creates a copy of this collection with a game removed
     * @param gameId The ID of the game to remove
     * @return A new GameCollection instance with the game removed
     */
    fun withGameRemoved(gameId: Int): GameCollection {
        return copy(
            gameIds = gameIds.filter { it != gameId },
            updatedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Sealed class representing validation results
 */
sealed class ValidationResult {
    object Valid : ValidationResult()
    data class Invalid(val message: String) : ValidationResult()
    
    fun isValid(): Boolean = this is Valid
    fun isInvalid(): Boolean = this is Invalid
    
    fun getErrorMessage(): String? = when (this) {
        is Invalid -> message
        is Valid -> null
    }
}