package com.devpush.kmp.collections

import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.repository.GameCollectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.*

/**
 * Fake implementation of GameCollectionRepository for testing purposes.
 * Provides in-memory storage and simulates database operations.
 */
class FakeGameCollectionRepository : GameCollectionRepository {
    
    private val collections = MutableStateFlow<Map<String, GameCollection>>(emptyMap())
    private val collectionGames = MutableStateFlow<Map<String, Set<Int>>>(emptyMap())
    
    override fun getCollections(): Flow<List<GameCollection>> {
        return collections.map { collectionsMap ->
            collectionsMap.values.map { collection ->
                collection.copy(gameIds = collectionGames.value[collection.id]?.toList() ?: emptyList())
            }.sortedWith(compareBy<GameCollection> { it.type != CollectionType.WISHLIST }
                .thenBy { it.type != CollectionType.CURRENTLY_PLAYING }
                .thenBy { it.type != CollectionType.COMPLETED }
                .thenBy { it.createdAt })
        }
    }
    
    override suspend fun getCollectionById(id: String): GameCollection? {
        val collection = collections.value[id] ?: return null
        val gameIds = collectionGames.value[id]?.toList() ?: emptyList()
        return collection.copy(gameIds = gameIds)
    }
    
    override suspend fun createCollection(
        name: String,
        description: String?,
        type: CollectionType
    ): String {
        // Validate name
        if (name.isBlank()) {
            throw IllegalArgumentException("Collection name cannot be empty")
        }
        if (name.length > 50) {
            throw IllegalArgumentException("Collection name cannot exceed 50 characters")
        }
        
        // Check for duplicate names
        if (collections.value.values.any { it.name == name }) {
            throw IllegalArgumentException("Collection with name '$name' already exists")
        }
        
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val collection = GameCollection(
            id = id,
            name = name,
            type = type,
            gameIds = emptyList(),
            createdAt = now,
            updatedAt = now,
            description = description
        )
        
        collections.value = collections.value + (id to collection)
        collectionGames.value = collectionGames.value + (id to emptySet())
        
        return id
    }
    
    override suspend fun updateCollection(
        id: String,
        name: String?,
        description: String?
    ) {
        val collection = collections.value[id] ?: throw IllegalArgumentException("Collection not found")
        
        // Validate name if provided
        name?.let { newName ->
            if (newName.isBlank()) {
                throw IllegalArgumentException("Collection name cannot be empty")
            }
            if (newName.length > 50) {
                throw IllegalArgumentException("Collection name cannot exceed 50 characters")
            }
            
            // Check for duplicate names (excluding current collection)
            if (collections.value.values.any { it.name == newName && it.id != id }) {
                throw IllegalArgumentException("Collection with name '$newName' already exists")
            }
        }
        
        val updatedCollection = collection.copy(
            name = name ?: collection.name,
            description = description ?: collection.description,
            updatedAt = System.currentTimeMillis()
        )
        
        collections.value = collections.value + (id to updatedCollection)
    }
    
    override suspend fun deleteCollection(id: String) {
        val collection = collections.value[id] ?: throw IllegalArgumentException("Collection not found")
        
        // Protect default collections
        if (collection.type != CollectionType.CUSTOM) {
            throw IllegalArgumentException("Cannot delete default collection")
        }
        
        collections.value = collections.value - id
        collectionGames.value = collectionGames.value - id
    }
    
    override suspend fun addGameToCollection(collectionId: String, gameId: Int) {
        if (collections.value[collectionId] == null) {
            throw IllegalArgumentException("Collection not found")
        }
        
        val currentGames = collectionGames.value[collectionId] ?: emptySet()
        
        // Don't add if already exists (idempotent operation)
        if (!currentGames.contains(gameId)) {
            collectionGames.value = collectionGames.value + (collectionId to currentGames + gameId)
            
            // Update collection timestamp
            val collection = collections.value[collectionId]!!
            collections.value = collections.value + (collectionId to collection.copy(
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
    
    override suspend fun removeGameFromCollection(collectionId: String, gameId: Int) {
        if (collections.value[collectionId] == null) {
            throw IllegalArgumentException("Collection not found")
        }
        
        val currentGames = collectionGames.value[collectionId] ?: emptySet()
        
        // Only remove if exists (idempotent operation)
        if (currentGames.contains(gameId)) {
            collectionGames.value = collectionGames.value + (collectionId to currentGames - gameId)
            
            // Update collection timestamp
            val collection = collections.value[collectionId]!!
            collections.value = collections.value + (collectionId to collection.copy(
                updatedAt = System.currentTimeMillis()
            ))
        }
    }
    
    override suspend fun getGamesInCollection(collectionId: String): List<Int> {
        return collectionGames.value[collectionId]?.toList() ?: emptyList()
    }
    
    override suspend fun isGameInCollection(collectionId: String, gameId: Int): Boolean {
        return collectionGames.value[collectionId]?.contains(gameId) ?: false
    }
    
    override suspend fun getCollectionsContainingGame(gameId: Int): List<GameCollection> {
        return collections.value.values.filter { collection ->
            collectionGames.value[collection.id]?.contains(gameId) == true
        }.map { collection ->
            collection.copy(gameIds = collectionGames.value[collection.id]?.toList() ?: emptyList())
        }
    }
    
    override suspend fun collectionExists(name: String): Boolean {
        return collections.value.values.any { it.name == name }
    }
    
    // Helper methods for testing
    fun clear() {
        collections.value = emptyMap()
        collectionGames.value = emptyMap()
    }
    
    fun getCollectionCount(): Int = collections.value.size
    
    fun getGameCount(collectionId: String): Int = collectionGames.value[collectionId]?.size ?: 0
}