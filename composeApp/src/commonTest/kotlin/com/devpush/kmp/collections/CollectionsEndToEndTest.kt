package com.devpush.kmp.collections

import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * End-to-end tests for the Game Collections feature.
 * Tests complete user flows from collections list to game management.
 */
class CollectionsEndToEndTest {

    private lateinit var repository: FakeGameCollectionRepository
    private lateinit var getCollectionsUseCase: GetCollectionsUseCase
    private lateinit var createCollectionUseCase: CreateCollectionUseCase
    private lateinit var deleteCollectionUseCase: DeleteCollectionUseCase
    private lateinit var addGameToCollectionUseCase: AddGameToCollectionUseCase
    private lateinit var removeGameFromCollectionUseCase: RemoveGameFromCollectionUseCase
    private lateinit var updateCollectionUseCase: UpdateCollectionUseCase
    private lateinit var initializeDefaultCollectionsUseCase: InitializeDefaultCollectionsUseCase

    @BeforeTest
    fun setup() {
        repository = FakeGameCollectionRepository()
        getCollectionsUseCase = GetCollectionsUseCase(repository)
        createCollectionUseCase = CreateCollectionUseCase(repository)
        deleteCollectionUseCase = DeleteCollectionUseCase(repository)
        addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCase(repository)
        updateCollectionUseCase = UpdateCollectionUseCase(repository)
        initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
    }

    @Test
    fun `complete user flow - initialize default collections and manage games`() = runTest {
        // Step 1: Initialize default collections (app startup)
        initializeDefaultCollectionsUseCase()
        
        val collections = getCollectionsUseCase().first()
        assertEquals(3, collections.size)
        assertTrue(collections.any { it.type == CollectionType.WISHLIST })
        assertTrue(collections.any { it.type == CollectionType.CURRENTLY_PLAYING })
        assertTrue(collections.any { it.type == CollectionType.COMPLETED })

        // Step 2: Add games to wishlist
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        addGameToCollectionUseCase(wishlist.id, 1)
        addGameToCollectionUseCase(wishlist.id, 2)
        addGameToCollectionUseCase(wishlist.id, 3)

        val updatedWishlist = getCollectionsUseCase().first()
            .first { it.type == CollectionType.WISHLIST }
        assertEquals(3, updatedWishlist.gameIds.size)
        assertTrue(updatedWishlist.gameIds.contains(1))
        assertTrue(updatedWishlist.gameIds.contains(2))
        assertTrue(updatedWishlist.gameIds.contains(3))

        // Step 3: Move game from wishlist to currently playing
        val currentlyPlaying = collections.first { it.type == CollectionType.CURRENTLY_PLAYING }
        removeGameFromCollectionUseCase(wishlist.id, 1)
        addGameToCollectionUseCase(currentlyPlaying.id, 1)

        val finalCollections = getCollectionsUseCase().first()
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        val finalCurrentlyPlaying = finalCollections.first { it.type == CollectionType.CURRENTLY_PLAYING }

        assertEquals(2, finalWishlist.gameIds.size)
        assertFalse(finalWishlist.gameIds.contains(1))
        assertEquals(1, finalCurrentlyPlaying.gameIds.size)
        assertTrue(finalCurrentlyPlaying.gameIds.contains(1))

        // Step 4: Complete the game
        val completed = collections.first { it.type == CollectionType.COMPLETED }
        removeGameFromCollectionUseCase(currentlyPlaying.id, 1)
        addGameToCollectionUseCase(completed.id, 1)

        val completedCollections = getCollectionsUseCase().first()
        val finalCompleted = completedCollections.first { it.type == CollectionType.COMPLETED }
        val finalCurrentlyPlayingAfterCompletion = completedCollections.first { it.type == CollectionType.CURRENTLY_PLAYING }

        assertEquals(0, finalCurrentlyPlayingAfterCompletion.gameIds.size)
        assertEquals(1, finalCompleted.gameIds.size)
        assertTrue(finalCompleted.gameIds.contains(1))
    }

    @Test
    fun `complete user flow - create and manage custom collection`() = runTest {
        // Step 1: Create custom collection
        val customCollectionId = createCollectionUseCase("My Favorites", "Games I really love", CollectionType.CUSTOM)
        assertNotNull(customCollectionId)

        val collections = getCollectionsUseCase().first()
        val customCollection = collections.first { it.id == customCollectionId }
        assertEquals("My Favorites", customCollection.name)
        assertEquals("Games I really love", customCollection.description)
        assertEquals(CollectionType.CUSTOM, customCollection.type)

        // Step 2: Add games to custom collection
        addGameToCollectionUseCase(customCollectionId, 10)
        addGameToCollectionUseCase(customCollectionId, 20)
        addGameToCollectionUseCase(customCollectionId, 30)

        val updatedCollection = getCollectionsUseCase().first()
            .first { it.id == customCollectionId }
        assertEquals(3, updatedCollection.gameIds.size)

        // Step 3: Update collection details
        updateCollectionUseCase(customCollectionId, "My Top Games", "The best games ever")

        val renamedCollection = getCollectionsUseCase().first()
            .first { it.id == customCollectionId }
        assertEquals("My Top Games", renamedCollection.name)
        assertEquals("The best games ever", renamedCollection.description)

        // Step 4: Remove some games
        removeGameFromCollectionUseCase(customCollectionId, 10)

        val finalCollection = getCollectionsUseCase().first()
            .first { it.id == customCollectionId }
        assertEquals(2, finalCollection.gameIds.size)
        assertFalse(finalCollection.gameIds.contains(10))

        // Step 5: Delete custom collection
        deleteCollectionUseCase(customCollectionId)

        val finalCollections = getCollectionsUseCase().first()
        assertFalse(finalCollections.any { it.id == customCollectionId })
    }

    @Test
    fun `error scenarios - duplicate games and invalid operations`() = runTest {
        // Initialize collections
        initializeDefaultCollectionsUseCase()
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }

        // Test duplicate game addition
        addGameToCollectionUseCase(wishlist.id, 1)
        
        // Attempting to add the same game again should not increase count
        addGameToCollectionUseCase(wishlist.id, 1)
        
        val updatedWishlist = getCollectionsUseCase().first()
            .first { it.type == CollectionType.WISHLIST }
        assertEquals(1, updatedWishlist.gameIds.size)

        // Test removing non-existent game
        removeGameFromCollectionUseCase(wishlist.id, 999)
        
        val finalWishlist = getCollectionsUseCase().first()
            .first { it.type == CollectionType.WISHLIST }
        assertEquals(1, finalWishlist.gameIds.size) // Should remain unchanged

        // Test deleting default collection (should be protected)
        assertFailsWith<IllegalArgumentException> {
            deleteCollectionUseCase(wishlist.id)
        }
    }

    @Test
    fun `performance test - large number of games and collections`() = runTest {
        // Initialize default collections
        initializeDefaultCollectionsUseCase()
        
        // Create multiple custom collections
        val customCollectionIds = mutableListOf<String>()
        repeat(10) { index ->
            val id = createCollectionUseCase("Collection $index", "Description $index", CollectionType.CUSTOM)
            customCollectionIds.add(id)
        }

        // Add many games to each collection
        customCollectionIds.forEach { collectionId ->
            repeat(100) { gameId ->
                addGameToCollectionUseCase(collectionId, gameId)
            }
        }

        // Verify all collections and games are properly managed
        val allCollections = getCollectionsUseCase().first()
        assertEquals(13, allCollections.size) // 3 default + 10 custom

        customCollectionIds.forEach { collectionId ->
            val collection = allCollections.first { it.id == collectionId }
            assertEquals(100, collection.gameIds.size)
        }

        // Test bulk removal
        customCollectionIds.forEach { collectionId ->
            repeat(50) { gameId ->
                removeGameFromCollectionUseCase(collectionId, gameId)
            }
        }

        val finalCollections = getCollectionsUseCase().first()
        customCollectionIds.forEach { collectionId ->
            val collection = finalCollections.first { it.id == collectionId }
            assertEquals(50, collection.gameIds.size)
        }
    }

    @Test
    fun `data integrity test - concurrent operations`() = runTest {
        initializeDefaultCollectionsUseCase()
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }

        // Simulate concurrent additions
        val gameIds = (1..20).toList()
        gameIds.forEach { gameId ->
            addGameToCollectionUseCase(wishlist.id, gameId)
        }

        val updatedWishlist = getCollectionsUseCase().first()
            .first { it.type == CollectionType.WISHLIST }
        assertEquals(20, updatedWishlist.gameIds.size)

        // Simulate concurrent removals
        gameIds.take(10).forEach { gameId ->
            removeGameFromCollectionUseCase(wishlist.id, gameId)
        }

        val finalWishlist = getCollectionsUseCase().first()
            .first { it.type == CollectionType.WISHLIST }
        assertEquals(10, finalWishlist.gameIds.size)
        
        // Verify remaining games are correct
        gameIds.drop(10).forEach { gameId ->
            assertTrue(finalWishlist.gameIds.contains(gameId))
        }
    }

    @Test
    fun `validation test - collection name constraints`() = runTest {
        // Test empty name
        assertFailsWith<IllegalArgumentException> {
            createCollectionUseCase("", "Valid description", CollectionType.CUSTOM)
        }

        // Test name too long
        val longName = "a".repeat(51)
        assertFailsWith<IllegalArgumentException> {
            createCollectionUseCase(longName, "Valid description", CollectionType.CUSTOM)
        }

        // Test duplicate name
        createCollectionUseCase("Test Collection", "Description", CollectionType.CUSTOM)
        assertFailsWith<IllegalArgumentException> {
            createCollectionUseCase("Test Collection", "Another description", CollectionType.CUSTOM)
        }

        // Test valid name
        val validId = createCollectionUseCase("Valid Collection", "Valid description", CollectionType.CUSTOM)
        assertNotNull(validId)
    }
}