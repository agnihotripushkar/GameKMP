package com.devpush.features.collections.domain.collections

import com.devpush.features.common.utils.StringUtils

/**
 * Comprehensive sealed class representing different types of collection operation errors
 * with specific error messages and recovery suggestions
 */
sealed class CollectionError : Exception() {
    abstract val userMessage: String
    abstract val technicalMessage: String
    abstract val canRetry: Boolean
    abstract val suggestedAction: String?
    
    // Collection not found errors
    data class CollectionNotFound(val collectionId: String) : CollectionError() {
        override val message: String = "Collection with ID '$collectionId' not found"
        override val userMessage: String = "Collection not found"
        override val technicalMessage: String = "Collection with ID '$collectionId' does not exist in database"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "The collection may have been deleted. Please refresh the collections list"
    }
    
    // Duplicate and validation errors
    data class CollectionNameExists(val name: String) : CollectionError() {
        override val message: String = "Collection with name '$name' already exists"
        override val userMessage: String = "Collection name already exists"
        override val technicalMessage: String = "Duplicate collection name: '$name'"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please choose a different name for your collection"
    }
    
    data class DuplicateGameInCollection(val gameId: Int, val collectionName: String) : CollectionError() {
        override val message: String = "Game $gameId is already in collection '$collectionName'"
        override val userMessage: String = "Game is already in this collection"
        override val technicalMessage: String = "Game ID $gameId already exists in collection '$collectionName'"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "This game is already in the collection"
    }
    
    data class GameNotInCollection(val gameId: Int, val collectionName: String) : CollectionError() {
        override val message: String = "Game $gameId is not in collection '$collectionName'"
        override val userMessage: String = "Game not found in collection"
        override val technicalMessage: String = "Game ID $gameId does not exist in collection '$collectionName'"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "The game may have already been removed from this collection"
    }
    
    // Validation errors
    data class ValidationError(val field: String, val reason: String = "") : CollectionError() {
        override val message: String = "Invalid $field${if (reason.isNotEmpty()) ": $reason" else ""}"
        override val userMessage: String = "Invalid input for $field"
        override val technicalMessage: String = "Validation failed for field: $field - $reason"
        override val canRetry: Boolean = false
        override val suggestedAction: String = when (with(StringUtils) { field.toLowerCaseCompat() }) {
            "collection name" -> "Please enter a valid collection name (2-50 characters, no special symbols)"
            "description" -> "Please enter a description with less than 200 characters"
            "game id" -> "Please select a valid game"
            "collection type" -> "Please select a valid collection type"
            else -> "Please check your input and try again"
        }
    }
    
    data class InvalidCollectionOperation(val operation: String, val reason: String) : CollectionError() {
        override val message: String = "Invalid operation '$operation': $reason"
        override val userMessage: String = "Operation not allowed"
        override val technicalMessage: String = "Collection operation '$operation' failed: $reason"
        override val canRetry: Boolean = false
        override val suggestedAction: String = when (with(StringUtils) { operation.toLowerCaseCompat() }) {
            "delete" -> "Cannot delete this collection. It may be a default collection or contain games"
            "rename" -> "Cannot rename this collection. It may be a default collection"
            "add game" -> "Cannot add this game to the collection"
            "remove game" -> "Cannot remove this game from the collection"
            else -> "This operation is not allowed for this collection"
        }
    }
    
    // Database-related errors
    object DatabaseError : CollectionError() {
        override val message: String = "Database error occurred"
        override val userMessage: String = "Data access failed"
        override val technicalMessage: String = "Collection database operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem accessing your collections. Please try again"
    }
    
    object DatabaseCorruptionError : CollectionError() {
        override val message: String = "Database corruption detected"
        override val userMessage: String = "Data corruption detected"
        override val technicalMessage: String = "Collection database integrity check failed"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please restart the app to rebuild the collections database"
    }
    
    data class DatabaseConstraintError(val constraint: String) : CollectionError() {
        override val message: String = "Database constraint violation: $constraint"
        override val userMessage: String = "Data integrity error"
        override val technicalMessage: String = "Database constraint '$constraint' violated"
        override val canRetry: Boolean = false
        override val suggestedAction: String = when (with(StringUtils) { constraint.toLowerCaseCompat() }) {
            "foreign key" -> "The referenced game or collection no longer exists"
            "unique" -> "This would create duplicate data"
            "not null" -> "Required information is missing"
            else -> "There was a data integrity issue. Please try again"
        }
    }
    
    // Collection limits and capacity errors
    object CollectionLimitExceeded : CollectionError() {
        override val message: String = "Maximum number of collections reached"
        override val userMessage: String = "Too many collections"
        override val technicalMessage: String = "User has reached maximum collection limit"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "You've reached the maximum number of collections. Please delete some collections first"
    }
    
    data class CollectionCapacityExceeded(val maxGames: Int) : CollectionError() {
        override val message: String = "Collection cannot hold more than $maxGames games"
        override val userMessage: String = "Collection is full"
        override val technicalMessage: String = "Collection has reached maximum capacity of $maxGames games"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "This collection is full. Please remove some games or create a new collection"
    }
    
    // Default collection protection errors
    data class DefaultCollectionProtected(val collectionType: CollectionType, val operation: String) : CollectionError() {
        override val message: String = "Cannot $operation default collection '${collectionType.displayName}'"
        override val userMessage: String = "Cannot modify default collection"
        override val technicalMessage: String = "Operation '$operation' not allowed on default collection type: $collectionType"
        override val canRetry: Boolean = false
        override val suggestedAction: String = when (with(StringUtils) { operation.toLowerCaseCompat() }) {
            "delete" -> "Default collections cannot be deleted. You can only remove games from them"
            "rename" -> "Default collections cannot be renamed"
            else -> "This operation is not allowed on default collections"
        }
    }
    
    // Confirmation required errors
    data class ConfirmationRequired(
        override val message: String,
        val gameId: Int,
        val fromCollectionId: String,
        val toCollectionId: String
    ) : CollectionError() {
        override val userMessage: String = message
        override val technicalMessage: String = "User confirmation required for game $gameId move from $fromCollectionId to $toCollectionId"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please confirm this action to proceed"
    }
    
    // Concurrent modification errors
    data class ConcurrentModificationError(val collectionId: String) : CollectionError() {
        override val message: String = "Collection '$collectionId' was modified by another process"
        override val userMessage: String = "Collection was modified elsewhere"
        override val technicalMessage: String = "Concurrent modification detected for collection '$collectionId'"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "The collection was updated elsewhere. Please refresh and try again"
    }
    
    // Import/Export errors
    object ImportError : CollectionError() {
        override val message: String = "Failed to import collections"
        override val userMessage: String = "Import failed"
        override val technicalMessage: String = "Collection import operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem importing your collections. Please check the file and try again"
    }
    
    object ExportError : CollectionError() {
        override val message: String = "Failed to export collections"
        override val userMessage: String = "Export failed"
        override val technicalMessage: String = "Collection export operation failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "There was a problem exporting your collections. Please try again"
    }
    
    // Network and sync errors (for future cloud sync)
    object SyncError : CollectionError() {
        override val message: String = "Failed to sync collections"
        override val userMessage: String = "Sync failed"
        override val technicalMessage: String = "Collection synchronization failed"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "Unable to sync your collections. Check your internet connection and try again"
    }
    
    data class SyncConflictError(val conflictCount: Int) : CollectionError() {
        override val message: String = "Sync conflicts detected for $conflictCount collections"
        override val userMessage: String = "Sync conflicts found"
        override val technicalMessage: String = "Collection sync conflicts: $conflictCount items"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Some collections have conflicts. Please review and resolve them manually"
    }
    
    // Performance and resource errors
    object PerformanceError : CollectionError() {
        override val message: String = "Collection operation taking too long"
        override val userMessage: String = "Operation is slow"
        override val technicalMessage: String = "Collection operation exceeded performance threshold"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "This is taking longer than usual. Please wait or try with fewer collections"
    }
    
    object MemoryError : CollectionError() {
        override val message: String = "Insufficient memory for collection operation"
        override val userMessage: String = "Not enough memory"
        override val technicalMessage: String = "Collection operation failed due to memory constraints"
        override val canRetry: Boolean = false
        override val suggestedAction: String = "Please close other apps and try again"
    }
    
    // Generic errors
    data class UnknownError(
        override val message: String,
        val originalException: Throwable? = null
    ) : CollectionError() {
        override val userMessage: String = "Something went wrong"
        override val technicalMessage: String = "Unexpected collection error: $message"
        override val canRetry: Boolean = true
        override val suggestedAction: String = "An unexpected error occurred with your collections. Please try again"
    }
    
    // Helper methods for error categorization
    fun isValidationError(): Boolean = when (this) {
        is ValidationError, is CollectionNameExists, is DuplicateGameInCollection, 
        is InvalidCollectionOperation -> true
        else -> false
    }
    
    fun isDatabaseError(): Boolean = when (this) {
        is DatabaseError, is DatabaseCorruptionError, is DatabaseConstraintError -> true
        else -> false
    }
    
    fun isCapacityError(): Boolean = when (this) {
        is CollectionLimitExceeded, is CollectionCapacityExceeded -> true
        else -> false
    }
    
    fun isProtectionError(): Boolean = when (this) {
        is DefaultCollectionProtected -> true
        else -> false
    }
    
    fun isSyncError(): Boolean = when (this) {
        is SyncError, is SyncConflictError -> true
        else -> false
    }
    
    fun isRecoverable(): Boolean = canRetry
    
    fun requiresUserAction(): Boolean = when (this) {
        is ValidationError, is CollectionNameExists, is InvalidCollectionOperation,
        is CollectionLimitExceeded, is CollectionCapacityExceeded,
        is DefaultCollectionProtected, is ConfirmationRequired -> true
        else -> false
    }
    
    fun isTemporary(): Boolean = when (this) {
        is PerformanceError, is ConcurrentModificationError, is SyncError -> true
        else -> false
    }
}