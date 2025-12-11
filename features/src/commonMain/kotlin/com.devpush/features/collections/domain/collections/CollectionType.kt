package com.devpush.features.collections.domain.collections

import com.devpush.features.common.utils.StringUtils

/**
 * Enum representing different types of game collections
 * 
 * @param displayName Human-readable name for the collection type
 * @param isDefault Whether this is a predefined default collection type
 * @param sortOrder Order for displaying collections (lower numbers appear first)
 * @param description Brief description of what this collection type represents
 */
enum class CollectionType(
    val displayName: String,
    val isDefault: Boolean,
    val sortOrder: Int,
    val description: String
) {
    /**
     * Games the user wants to play in the future
     */
    WISHLIST(
        displayName = "Wishlist",
        isDefault = true,
        sortOrder = 1,
        description = "Games you want to play"
    ),
    
    /**
     * Games the user is currently playing
     */
    CURRENTLY_PLAYING(
        displayName = "Currently Playing",
        isDefault = true,
        sortOrder = 2,
        description = "Games you're playing right now"
    ),
    
    /**
     * Games the user has finished playing
     */
    COMPLETED(
        displayName = "Completed",
        isDefault = true,
        sortOrder = 3,
        description = "Games you've finished"
    ),
    
    /**
     * Custom user-defined collection
     */
    CUSTOM(
        displayName = "Custom",
        isDefault = false,
        sortOrder = 100,
        description = "Custom collection"
    );
    
    companion object {
        /**
         * Gets all default collection types
         * @return List of default collection types sorted by sort order
         */
        fun getDefaultTypes(): List<CollectionType> {
            return values().filter { it.isDefault }.sortedBy { it.sortOrder }
        }
        
        /**
         * Gets all collection types sorted by sort order
         * @return List of all collection types sorted by sort order
         */
        fun getAllTypesSorted(): List<CollectionType> {
            return values().sortedBy { it.sortOrder }
        }
        
        /**
         * Finds a collection type by its display name (case-insensitive)
         * @param displayName The display name to search for
         * @return The matching CollectionType or null if not found
         */
        fun findByDisplayName(displayName: String): CollectionType? {
            return values().find { 
                with(StringUtils) { it.displayName.equalsIgnoreCase(displayName) }
            }
        }
        
        /**
         * Checks if a display name corresponds to a default collection type
         * @param displayName The display name to check
         * @return true if it's a default type, false otherwise
         */
        fun isDefaultType(displayName: String): Boolean {
            return findByDisplayName(displayName)?.isDefault == true
        }
        
        /**
         * Gets the appropriate collection type for a custom collection
         * @return CollectionType.CUSTOM
         */
        fun getCustomType(): CollectionType = CUSTOM
        
        /**
         * Validates if a collection type is appropriate for the given name
         * @param type The collection type
         * @param name The collection name
         * @return true if the combination is valid, false otherwise
         */
        fun isValidTypeForName(type: CollectionType, name: String): Boolean {
            return when (type) {
                CUSTOM -> !getDefaultTypes().any { 
                    with(StringUtils) { it.displayName.equalsIgnoreCase(name) }
                }
                else -> with(StringUtils) { type.displayName.equalsIgnoreCase(name) }
            }
        }
    }
    
    /**
     * Checks if this collection type represents a gaming status
     * (wishlist, currently playing, completed)
     * @return true if this is a status collection, false otherwise
     */
    fun isStatusCollection(): Boolean {
        return this in listOf(WISHLIST, CURRENTLY_PLAYING, COMPLETED)
    }
    
    /**
     * Gets the next logical status in the gaming progression
     * @return The next status collection type, or null if no logical next step
     */
    fun getNextStatus(): CollectionType? {
        return when (this) {
            WISHLIST -> CURRENTLY_PLAYING
            CURRENTLY_PLAYING -> COMPLETED
            else -> null
        }
    }
    
    /**
     * Gets the previous logical status in the gaming progression
     * @return The previous status collection type, or null if no logical previous step
     */
    fun getPreviousStatus(): CollectionType? {
        return when (this) {
            COMPLETED -> CURRENTLY_PLAYING
            CURRENTLY_PLAYING -> WISHLIST
            else -> null
        }
    }
    
    /**
     * Checks if moving from this type to another type requires user confirmation
     * @param targetType The type to move to
     * @return true if confirmation is needed, false otherwise
     */
    fun requiresConfirmationToMoveTo(targetType: CollectionType): Boolean {
        return when {
            this == targetType -> false
            !this.isStatusCollection() || !targetType.isStatusCollection() -> false
            this == WISHLIST && targetType == COMPLETED -> true
            this == COMPLETED && targetType == WISHLIST -> true
            else -> false
        }
    }
    
    /**
     * Gets a user-friendly message for moving between collection types
     * @param targetType The type to move to
     * @return A message explaining the move, or null if no special message needed
     */
    fun getMoveMessage(targetType: CollectionType): String? {
        return when {
            this == WISHLIST && targetType == CURRENTLY_PLAYING -> 
                "Move from Wishlist to Currently Playing?"
            this == CURRENTLY_PLAYING && targetType == COMPLETED -> 
                "Mark as completed and remove from Currently Playing?"
            this == WISHLIST && targetType == COMPLETED -> 
                "Mark as completed without playing?"
            this == COMPLETED && targetType == WISHLIST -> 
                "Move back to Wishlist? This will remove it from Completed."
            this == COMPLETED && targetType == CURRENTLY_PLAYING -> 
                "Play again? This will remove it from Completed."
            else -> null
        }
    }
}