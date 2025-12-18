package com.devpush.features.game.domain.usecase

import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.collections.domain.collections.CollectionError
import com.devpush.features.game.domain.repository.GameCollectionRepository
import com.devpush.features.common.utils.StringUtils
import kotlinx.datetime.Clock


/**
 * Use case for retrieving collections with game counts, sorting, and caching
 */
interface GetCollectionsUseCase {
    /**
     * Gets all collections with proper sorting (default types first)
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     * @return Result containing sorted list of GameCollections with game counts or error
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<CollectionWithCount>>
    
    /**
     * Gets collections by type
     * @param type The collection type to filter by
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     * @return Result containing list of GameCollections of the specified type or error
     */
    suspend fun getCollectionsByType(
        type: CollectionType,
        forceRefresh: Boolean = false
    ): Result<List<CollectionWithCount>>
    
    /**
     * Gets a specific collection by ID with game count
     * @param collectionId The ID of the collection to retrieve
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     * @return Result containing the GameCollection with count or error
     */
    suspend fun getCollectionById(
        collectionId: String,
        forceRefresh: Boolean = false
    ): Result<CollectionWithCount>
    
    /**
     * Clears the collections cache
     */
    suspend fun clearCache()
}

/**
 * Data class representing a collection with its game count
 */
data class CollectionWithCount(
    val collection: GameCollection,
    val gameCount: Int
) {
    val id: String get() = collection.id
    val name: String get() = collection.name
    val type: CollectionType get() = collection.type
    val createdAt: Long get() = collection.createdAt
    val updatedAt: Long get() = collection.updatedAt
    val description: String? get() = collection.description
    val isEmpty: Boolean get() = gameCount == 0
}

class GetCollectionsUseCaseImpl(
    private val repository: GameCollectionRepository
) : GetCollectionsUseCase {
    
    // Simple in-memory cache
    private var cachedCollections: List<CollectionWithCount>? = null
    private var cacheTimestamp: Long = 0
    private val cacheValidityDuration = 30_000L // 30 seconds
    
    override suspend fun invoke(forceRefresh: Boolean): Result<List<CollectionWithCount>> {
        return try {
            // Check cache validity
            if (!forceRefresh && isCacheValid()) {
                cachedCollections?.let { cached ->
                    return Result.success(cached)
                }
            }
            
            // Fetch collections from repository
            val collectionsResult = repository.getCollectionsSorted(
                sortByType = true,
                sortByName = true,
                sortByGameCount = false,
                ascending = true
            )
            
            val collections = collectionsResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            // Get game counts for all collections
            val gameCountsResult = repository.getCollectionGameCounts()
            val gameCounts = gameCountsResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            // Combine collections with their counts
            val collectionsWithCounts = collections.map { collection ->
                CollectionWithCount(
                    collection = collection,
                    gameCount = gameCounts[collection.id] ?: 0
                )
            }
            
            // Sort collections: default types first, then by name
            val sortedCollections = collectionsWithCounts.sortedWith(
                compareBy<CollectionWithCount> { !it.type.isDefault }
                    .thenBy { it.type.sortOrder }
                    .thenBy { with(StringUtils) { it.name.toLowerCaseCompat() } }
            )
            
            // Update cache
            cachedCollections = sortedCollections
            cacheTimestamp = Clock.System.now().toEpochMilliseconds()
            
            Result.success(sortedCollections)

            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get collections: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getCollectionsByType(
        type: CollectionType,
        forceRefresh: Boolean
    ): Result<List<CollectionWithCount>> {
        return try {
            // Get all collections first
            val allCollectionsResult = invoke(forceRefresh)
            val allCollections = allCollectionsResult.getOrElse { error ->
                return Result.failure(error)
            }
            
            // Filter by type
            val filteredCollections = allCollections.filter { it.type == type }
            
            Result.success(filteredCollections)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get collections by type: ${e.message}", e)
            )
        }
    }
    
    override suspend fun getCollectionById(
        collectionId: String,
        forceRefresh: Boolean
    ): Result<CollectionWithCount> {
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
            
            // Try to find in cache first if not forcing refresh
            if (!forceRefresh && isCacheValid()) {
                cachedCollections?.find { it.id == collectionId }?.let { cached ->
                    return Result.success(cached)
                }
            }
            
            // Fetch from repository
            val collectionResult = repository.getCollectionById(collectionId)
            val collection = collectionResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.CollectionNotFound(collectionId)
                )
            }
            
            // Get game count for this collection
            val gameCountsResult = repository.getCollectionGameCounts()
            val gameCounts = gameCountsResult.getOrElse { error ->
                return Result.failure(
                    if (error is CollectionError) error
                    else CollectionError.DatabaseError
                )
            }
            
            val collectionWithCount = CollectionWithCount(
                collection = collection,
                gameCount = gameCounts[collection.id] ?: 0
            )
            
            Result.success(collectionWithCount)
            
        } catch (e: Exception) {
            Result.failure(
                if (e is CollectionError) e
                else CollectionError.UnknownError("Failed to get collection by ID: ${e.message}", e)
            )
        }
    }
    
    override suspend fun clearCache() {
        cachedCollections = null
        cacheTimestamp = 0
    }
    
    /**
     * Checks if the current cache is still valid
     */
    private fun isCacheValid(): Boolean {
        return cachedCollections != null && 
               (Clock.System.now().toEpochMilliseconds() - cacheTimestamp) < cacheValidityDuration
    }

}