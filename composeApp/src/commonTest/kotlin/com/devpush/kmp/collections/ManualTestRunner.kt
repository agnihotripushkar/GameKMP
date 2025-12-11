package com.devpush.kmp.collections

import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * Manual test runner for collections functionality.
 * This can be used to manually verify the collections feature works correctly.
 */
object ManualTestRunner {
    
    fun runAllTests() {
        println("Starting Collections Feature Manual Tests...")
        
        try {
            testBasicCollectionOperations()
            testGameManagement()
            testErrorHandling()
            testPerformance()
            
            println("✅ All manual tests passed!")
        } catch (e: Exception) {
            println("❌ Test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun testBasicCollectionOperations() = runBlocking {
        println("\n🧪 Testing Basic Collection Operations...")
        
        val repository = FakeGameCollectionRepository()
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val createCollectionUseCase = CreateCollectionUseCase(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
        
        // Test initialization
        initializeDefaultCollectionsUseCase()
        val collections = getCollectionsUseCase().first()
        assert(collections.size == 3) { "Expected 3 default collections, got ${collections.size}" }
        
        // Test custom collection creation
        val customId = createCollectionUseCase("Test Collection", "Test Description", CollectionType.CUSTOM)
        val updatedCollections = getCollectionsUseCase().first()
        assert(updatedCollections.size == 4) { "Expected 4 collections after creation, got ${updatedCollections.size}" }
        
        val customCollection = updatedCollections.first { it.id == customId }
        assert(customCollection.name == "Test Collection") { "Collection name mismatch" }
        assert(customCollection.type == CollectionType.CUSTOM) { "Collection type mismatch" }
        
        println("✅ Basic collection operations test passed")
    }
    
    private fun testGameManagement() = runBlocking {
        println("\n🧪 Testing Game Management...")
        
        val repository = FakeGameCollectionRepository()
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        val removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCase(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
        
        // Initialize and get wishlist
        initializeDefaultCollectionsUseCase()
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        
        // Add games
        addGameToCollectionUseCase(wishlist.id, 1)
        addGameToCollectionUseCase(wishlist.id, 2)
        addGameToCollectionUseCase(wishlist.id, 3)
        
        val updatedCollections = getCollectionsUseCase().first()
        val updatedWishlist = updatedCollections.first { it.type == CollectionType.WISHLIST }
        assert(updatedWishlist.gameIds.size == 3) { "Expected 3 games, got ${updatedWishlist.gameIds.size}" }
        
        // Remove a game
        removeGameFromCollectionUseCase(wishlist.id, 2)
        val finalCollections = getCollectionsUseCase().first()
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        assert(finalWishlist.gameIds.size == 2) { "Expected 2 games after removal, got ${finalWishlist.gameIds.size}" }
        assert(!finalWishlist.gameIds.contains(2)) { "Game 2 should have been removed" }
        
        println("✅ Game management test passed")
    }
    
    private fun testErrorHandling() = runBlocking {
        println("\n🧪 Testing Error Handling...")
        
        val repository = FakeGameCollectionRepository()
        val createCollectionUseCase = CreateCollectionUseCase(repository)
        val deleteCollectionUseCase = DeleteCollectionUseCase(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
        
        // Test invalid collection name
        try {
            createCollectionUseCase("", "Description", CollectionType.CUSTOM)
            assert(false) { "Should have thrown exception for empty name" }
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Test duplicate collection name
        createCollectionUseCase("Duplicate", "First", CollectionType.CUSTOM)
        try {
            createCollectionUseCase("Duplicate", "Second", CollectionType.CUSTOM)
            assert(false) { "Should have thrown exception for duplicate name" }
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        // Test deleting default collection
        initializeDefaultCollectionsUseCase()
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        
        try {
            deleteCollectionUseCase(wishlist.id)
            assert(false) { "Should have thrown exception for deleting default collection" }
        } catch (e: IllegalArgumentException) {
            // Expected
        }
        
        println("✅ Error handling test passed")
    }
    
    private fun testPerformance() = runBlocking {
        println("\n🧪 Testing Performance...")
        
        val repository = FakeGameCollectionRepository()
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val createCollectionUseCase = CreateCollectionUseCase(repository)
        val addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
        
        initializeDefaultCollectionsUseCase()
        
        // Test creating many collections
        val startTime = System.currentTimeMillis()
        repeat(50) { index ->
            createCollectionUseCase("Collection $index", "Description $index", CollectionType.CUSTOM)
        }
        val creationTime = System.currentTimeMillis() - startTime
        println("Created 50 collections in ${creationTime}ms")
        
        // Test adding many games
        val collections = getCollectionsUseCase().first()
        val wishlist = collections.first { it.type == CollectionType.WISHLIST }
        
        val gameAdditionStart = System.currentTimeMillis()
        repeat(100) { gameId ->
            addGameToCollectionUseCase(wishlist.id, gameId)
        }
        val gameAdditionTime = System.currentTimeMillis() - gameAdditionStart
        println("Added 100 games in ${gameAdditionTime}ms")
        
        // Verify final state
        val finalCollections = getCollectionsUseCase().first()
        assert(finalCollections.size == 53) { "Expected 53 collections (3 default + 50 custom)" }
        
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        assert(finalWishlist.gameIds.size == 100) { "Expected 100 games in wishlist" }
        
        println("✅ Performance test passed")
    }
}

// Uncomment to run manual tests
// fun main() {
//     ManualTestRunner.runAllTests()
// }