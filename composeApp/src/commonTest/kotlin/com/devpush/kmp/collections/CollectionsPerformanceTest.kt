package com.devpush.kmp.collections

import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.system.measureTimeMillis
import kotlin.test.*

/**
 * Performance tests for the Game Collections feature.
 * Tests performance under various data loads and scenarios.
 */
class CollectionsPerformanceTest {

    private lateinit var repository: FakeGameCollectionRepository
    private lateinit var getCollectionsUseCase: GetCollectionsUseCase
    private lateinit var createCollectionUseCase: CreateCollectionUseCase
    private lateinit var addGameToCollectionUseCase: AddGameToCollectionUseCase
    private lateinit var removeGameFromCollectionUseCase: RemoveGameFromCollectionUseCase
    private lateinit var initializeDefaultCollectionsUseCase: InitializeDefaultCollectionsUseCase

    @BeforeTest
    fun setup() {
        repository = FakeGameCollectionRepository()
        getCollectionsUseCase = GetCollectionsUseCase(repository)
        createCollectionUseCase = CreateCollectionUseCase(repository)
        addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCase(repository)
        initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
    }

    @Test
    fun `performance test - loading large number of collections`() = runTest {
        // Initialize default collections
        initializeDefaultCollectionsUseCase()
        
        // Create many collections
        val collectionCount = 100
        val creationTime = measureTimeMillis {
            repeat(collectionCount) { index ->
                createCollectionUseCase("Collection $index", "Description $index", CollectionType.CUSTOM)
            }
        }
        
        println("Created $collectionCount collections in ${creationTime}ms")
        assertTrue(creationTime < 5000, "Collection creation took too long: ${creationTime}ms")
        
        // Test loading all collections
        val loadingTime = measureTimeMillis {
            val collections = getCollectionsUseCase().first()
            assertEquals(collectionCount + 3, collections.size) // +3 for default collections
        }
        
        println("Loaded ${collectionCount + 3} collections in ${loadingTime}ms")
        assertTrue(loadingTime < 1000, "Collection loading took too long: ${loadingTime}ms")
    }

    @Test
    fun `performance test - managing large number of games in collection`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        
        // Add many games to collection
        val gameCount = 1000
        val additionTime = measureTimeMillis {
            repeat(gameCount) { gameId ->
                addGameToCollectionUseCase(wishlist.id, gameId)
            }
        }
        
        println("Added $gameCount games in ${additionTime}ms")
        assertTrue(additionTime < 10000, "Game addition took too long: ${additionTime}ms")
        
        // Verify all games were added
        val verificationTime = measureTimeMillis {
            val updatedCollections = getCollectionsUseCase().first()
            val updatedWishlist = updatedCollections.first { it.type == CollectionType.WISHLIST }
            assertEquals(gameCount, updatedWishlist.gameIds.size)
        }
        
        println("Verified $gameCount games in ${verificationTime}ms")
        assertTrue(verificationTime < 1000, "Game verification took too long: ${verificationTime}ms")
        
        // Remove half of the games
        val removalTime = measureTimeMillis {
            repeat(gameCount / 2) { gameId ->
                removeGameFromCollectionUseCase(wishlist.id, gameId)
            }
        }
        
        println("Removed ${gameCount / 2} games in ${removalTime}ms")
        assertTrue(removalTime < 5000, "Game removal took too long: ${removalTime}ms")
        
        // Verify final count
        val finalCollections = getCollectionsUseCase().first()
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        assertEquals(gameCount / 2, finalWishlist.gameIds.size)
    }

    @Test
    fun `performance test - concurrent collection operations`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        val currentlyPlaying = collections.first { it.type == CollectionType.CURRENTLY_PLAYING }
        val completed = collections.first { it.type == CollectionType.COMPLETED }
        
        // Simulate concurrent operations across multiple collections
        val operationTime = measureTimeMillis {
            repeat(100) { index ->
                // Add games to different collections
                addGameToCollectionUseCase(wishlist.id, index)
                addGameToCollectionUseCase(currentlyPlaying.id, index + 1000)
                addGameToCollectionUseCase(completed.id, index + 2000)
                
                // Move some games between collections
                if (index > 10) {
                    removeGameFromCollectionUseCase(wishlist.id, index - 10)
                    addGameToCollectionUseCase(currentlyPlaying.id, index - 10)
                }
            }
        }
        
        println("Performed 500+ concurrent operations in ${operationTime}ms")
        assertTrue(operationTime < 15000, "Concurrent operations took too long: ${operationTime}ms")
        
        // Verify final state
        val finalCollections = getCollectionsUseCase().first()
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        val finalCurrentlyPlaying = finalCollections.first { it.type == CollectionType.CURRENTLY_PLAYING }
        val finalCompleted = finalCollections.first { it.type == CollectionType.COMPLETED }
        
        assertEquals(90, finalWishlist.gameIds.size) // 100 - 10 moved
        assertEquals(110, finalCurrentlyPlaying.gameIds.size) // 100 + 10 moved
        assertEquals(100, finalCompleted.gameIds.size)
    }

    @Test
    fun `performance test - frequent collection queries`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        // Create some collections with games
        val customCollectionId = createCollectionUseCase("Test Collection", "Test", CollectionType.CUSTOM)
        repeat(50) { gameId ->
            addGameToCollectionUseCase(customCollectionId, gameId)
        }
        
        // Perform many queries
        val queryCount = 1000
        val queryTime = measureTimeMillis {
            repeat(queryCount) {
                val collections = getCollectionsUseCase().first()
                assertTrue(collections.isNotEmpty())
            }
        }
        
        println("Performed $queryCount queries in ${queryTime}ms")
        assertTrue(queryTime < 5000, "Frequent queries took too long: ${queryTime}ms")
        
        val averageQueryTime = queryTime.toDouble() / queryCount
        println("Average query time: ${averageQueryTime}ms")
        assertTrue(averageQueryTime < 5.0, "Average query time too high: ${averageQueryTime}ms")
    }

    @Test
    fun `performance test - memory usage with large datasets`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        // Create many collections with many games each
        val collectionCount = 50
        val gamesPerCollection = 200
        
        val setupTime = measureTimeMillis {
            repeat(collectionCount) { collectionIndex ->
                val collectionId = createCollectionUseCase(
                    "Collection $collectionIndex", 
                    "Description $collectionIndex", 
                    CollectionType.CUSTOM
                )
                
                repeat(gamesPerCollection) { gameIndex ->
                    val gameId = collectionIndex * 1000 + gameIndex
                    addGameToCollectionUseCase(collectionId, gameId)
                }
            }
        }
        
        println("Created $collectionCount collections with $gamesPerCollection games each in ${setupTime}ms")
        
        // Test memory efficiency by loading all data multiple times
        val loadTestTime = measureTimeMillis {
            repeat(10) {
                val collections = getCollectionsUseCase().first()
                assertEquals(collectionCount + 3, collections.size) // +3 for defaults
                
                val totalGames = collections.sumOf { it.gameIds.size }
                assertEquals(collectionCount * gamesPerCollection, totalGames)
            }
        }
        
        println("Performed 10 full data loads in ${loadTestTime}ms")
        assertTrue(loadTestTime < 10000, "Memory load test took too long: ${loadTestTime}ms")
    }

    @Test
    fun `performance test - batch operations efficiency`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        
        // Test individual operations vs batch-like operations
        val individualOperationTime = measureTimeMillis {
            repeat(100) { gameId ->
                addGameToCollectionUseCase(wishlist.id, gameId)
                // Simulate checking state after each operation
                val currentCollections = getCollectionsUseCase().first()
                val currentWishlist = currentCollections.first { it.type == CollectionType.WISHLIST }
                assertTrue(currentWishlist.gameIds.contains(gameId))
            }
        }
        
        println("Individual operations with verification: ${individualOperationTime}ms")
        
        // Clear and test batch-like operations (without intermediate verification)
        repository.clear()
        initializeDefaultCollectionsUseCase()
        val newCollections = getCollectionsUseCase().first()
        val newWishlist = newCollections.first { it.type == CollectionType.WISHLIST }
        
        val batchOperationTime = measureTimeMillis {
            repeat(100) { gameId ->
                addGameToCollectionUseCase(newWishlist.id, gameId + 1000)
            }
            // Single verification at the end
            val finalCollections = getCollectionsUseCase().first()
            val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
            assertEquals(100, finalWishlist.gameIds.size)
        }
        
        println("Batch operations with single verification: ${batchOperationTime}ms")
        
        // Batch operations should be significantly faster
        assertTrue(batchOperationTime < individualOperationTime / 2, 
            "Batch operations should be more efficient than individual operations")
    }

    @Test
    fun `performance test - collection sorting and filtering`() = runTest {
        initializeDefaultCollectionsUseCase()
        
        // Create collections with different creation times
        val collectionIds = mutableListOf<String>()
        repeat(100) { index ->
            val id = createCollectionUseCase("Collection $index", "Description $index", CollectionType.CUSTOM)
            collectionIds.add(id)
            
            // Add different numbers of games to each collection
            repeat(index % 20) { gameId ->
                addGameToCollectionUseCase(id, gameId + index * 100)
            }
        }
        
        // Test sorting performance
        val sortingTime = measureTimeMillis {
            repeat(50) {
                val collections = getCollectionsUseCase().first()
                
                // Verify sorting (default collections first, then custom by creation time)
                assertEquals(CollectionType.WISHLIST, collections[0].type)
                assertEquals(CollectionType.CURRENTLY_PLAYING, collections[1].type)
                assertEquals(CollectionType.COMPLETED, collections[2].type)
                
                // Custom collections should maintain creation order
                val customCollections = collections.drop(3)
                assertEquals(100, customCollections.size)
                
                for (i in 1 until customCollections.size) {
                    assertTrue(customCollections[i-1].createdAt <= customCollections[i].createdAt,
                        "Collections should be sorted by creation time")
                }
            }
        }
        
        println("Sorting verification performed 50 times in ${sortingTime}ms")
        assertTrue(sortingTime < 2000, "Sorting performance is too slow: ${sortingTime}ms")
    }
}