package com.devpush.features.game.domain.repository

import com.devpush.features.game.domain.model.Game
import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType

/**
 * Repository interface for game collection operations
 * Provides methods for managing game collections with proper error handling
 */
interface GameCollectionRepository {
    
    /**
     * Creates a new game collection
     * @param name The name of the collection
     * @param type The type of collection (predefined or custom)
     * @param description Optional description for the collection
     * @return Result containing the created GameCollection or error
     */
    suspend fun createCollection(
        name: String, 
        type: CollectionType, 
        description: String? = null
    ): Result<GameCollection>
    
    /**
     * Retrieves all game collections
     * @return Result containing list of all GameCollections or error
     */
    suspend fun getAllCollections(): Result<List<GameCollection>>
    
    /**
     * Retrieves a specific collection by its ID
     * @param id The unique identifier of the collection
     * @return Result containing the GameCollection or error if not found
     */
    suspend fun getCollectionById(id: String): Result<GameCollection>
    
    /**
     * Retrieves collections by type
     * @param type The collection type to filter by
     * @return Result containing list of GameCollections of the specified type or error
     */
    suspend fun getCollectionsByType(type: CollectionType): Result<List<GameCollection>>
    
    /**
     * Adds a game to a collection
     * @param collectionId The ID of the collection to add the game to
     * @param gameId The ID of the game to add
     * @return Result indicating success or error (e.g., duplicate game, collection not found)
     */
    suspend fun addGameToCollection(collectionId: String, gameId: Int): Result<Unit>
    
    /**
     * Removes a game from a collection
     * @param collectionId The ID of the collection to remove the game from
     * @param gameId The ID of the game to remove
     * @return Result indicating success or error (e.g., game not in collection, collection not found)
     */
    suspend fun removeGameFromCollection(collectionId: String, gameId: Int): Result<Unit>
    
    /**
     * Deletes a collection and all its game associations
     * @param id The ID of the collection to delete
     * @return Result indicating success or error (e.g., collection not found, protected collection)
     */
    suspend fun deleteCollection(id: String): Result<Unit>
    
    /**
     * Updates an existing collection
     * @param collection The updated collection data
     * @return Result containing the updated GameCollection or error
     */
    suspend fun updateCollection(collection: GameCollection): Result<GameCollection>
    
    /**
     * Retrieves all games in a specific collection
     * @param collectionId The ID of the collection
     * @return Result containing list of Games in the collection or error
     */
    suspend fun getGamesInCollection(collectionId: String): Result<List<Game>>
    
    /**
     * Checks if a game exists in any collection
     * @param gameId The ID of the game to check
     * @return Result containing list of collection IDs that contain the game or error
     */
    suspend fun getCollectionsContainingGame(gameId: Int): Result<List<String>>
    
    /**
     * Moves a game from one collection to another
     * @param gameId The ID of the game to move
     * @param fromCollectionId The ID of the source collection
     * @param toCollectionId The ID of the destination collection
     * @return Result indicating success or error
     */
    suspend fun moveGameBetweenCollections(
        gameId: Int, 
        fromCollectionId: String, 
        toCollectionId: String
    ): Result<Unit>
    
    /**
     * Batch adds multiple games to a collection
     * @param collectionId The ID of the collection
     * @param gameIds List of game IDs to add
     * @return Result containing list of successfully added game IDs or error
     */
    suspend fun addGamesToCollection(
        collectionId: String, 
        gameIds: List<Int>
    ): Result<List<Int>>
    
    /**
     * Batch removes multiple games from a collection
     * @param collectionId The ID of the collection
     * @param gameIds List of game IDs to remove
     * @return Result containing list of successfully removed game IDs or error
     */
    suspend fun removeGamesFromCollection(
        collectionId: String, 
        gameIds: List<Int>
    ): Result<List<Int>>
    
    /**
     * Checks if a collection name already exists
     * @param name The collection name to check
     * @param excludeId Optional collection ID to exclude from the check (for updates)
     * @return Result containing true if name exists, false otherwise, or error
     */
    suspend fun collectionNameExists(name: String, excludeId: String? = null): Result<Boolean>
    
    /**
     * Gets the count of games in each collection
     * @return Result containing map of collection ID to game count or error
     */
    suspend fun getCollectionGameCounts(): Result<Map<String, Int>>
    
    /**
     * Initializes default collections if they don't exist
     * @return Result containing list of created default collections or error
     */
    suspend fun initializeDefaultCollections(): Result<List<GameCollection>>
    
    /**
     * Clears all games from a collection without deleting the collection
     * @param collectionId The ID of the collection to clear
     * @return Result indicating success or error
     */
    suspend fun clearCollection(collectionId: String): Result<Unit>
    
    /**
     * Gets collections sorted by specified criteria
     * @param sortByType Whether to sort by collection type first (default types first)
     * @param sortByName Whether to sort by name within each type group
     * @param sortByGameCount Whether to sort by game count
     * @param ascending Whether to sort in ascending order
     * @return Result containing sorted list of GameCollections or error
     */
    suspend fun getCollectionsSorted(
        sortByType: Boolean = true,
        sortByName: Boolean = true,
        sortByGameCount: Boolean = false,
        ascending: Boolean = true
    ): Result<List<GameCollection>>
}