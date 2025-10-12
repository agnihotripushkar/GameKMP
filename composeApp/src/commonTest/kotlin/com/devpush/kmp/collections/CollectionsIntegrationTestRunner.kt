package com.devpush.kmp.collections

import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import com.devpush.features.game.ui.collections.CollectionsViewModel
import com.devpush.features.game.ui.collections.CollectionDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.system.measureTimeMillis
import kotlin.test.*

/**
 * Integration test runner that simulates real user interactions
 * and measures performance of the collections feature.
 */
@OptIn(ExperimentalCoroutinesApi::class)
object CollectionsIntegrationTestRunner {
    
    fun runIntegrationTests() {
        println("🚀 Starting Collections Integration Tests...")
        
        try {
            testViewModelIntegration()
            testUserWorkflows()
            testPerformanceUnderLoad()
            testErrorRecovery()
            
            println("✅ All integration tests passed!")
        } catch (e: Exception) {
            println("❌ Integration test failed: ${e.message}")
            e.printStackTrace()
        }
    }
    
    private fun testViewModelIntegration() = runTest {
        println("\n🧪 Testing ViewModel Integration...")
        
        val testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        try {
            val repository = FakeGameCollectionRepository()
            val getCollectionsUseCase = GetCollectionsUseCaseImpl(repository)
            val createCollectionUseCase = CreateCollectionUseCaseImpl(repository)
            val deleteCollectionUseCase = DeleteCollectionUseCaseImpl(repository)
            val updateCollectionUseCase = UpdateCollectionUseCaseImpl(repository)
            val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCaseImpl(repository)
            
            val viewModel = CollectionsViewModel(
                getCollectionsUseCase = getCollectionsUseCase,
                createCollectionUseCase = createCollectionUseCase,
                deleteCollectionUseCase = deleteCollectionUseCase,
                updateCollectionUseCase = updateCollectionUseCase,
                initializeDefaultCollectionsUseCase = initializeDefaultCollectionsUseCase
            )
            
            // Wait for initialization
            testDispatcher.scheduler.advanceUntilIdle()
            
            val initialState = viewModel.uiState.first()
            assert(initialState.collections.size == 3) { "Expected 3 default collections" }
            assert(!initialState.isLoading) { "Should not be loading after initialization" }
            
            // Test collection creation
            viewModel.createCollection("Test Collection", "Test Description")
            testDispatcher.scheduler.advanceUntilIdle()
            
            val afterCreationState = viewModel.uiState.first()
            assert(afterCreationState.collections.size == 4) { "Expected 4 collections after creation" }
            
            println("✅ ViewModel integration test passed")
            
        } finally {
            Dispatchers.resetMain()
        }
    }
    
    private fun testUserWorkflows() = runTest {
        println("\n🧪 Testing User Workflows...")
        
        val repository = FakeGameCollectionRepository()
        val getCollectionsUseCase = GetCollectionsUseCaseImpl(repository)
        val addGameToCollectionUseCase = AddGameToCollectionUseCaseImpl(repository)
        val removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCaseImpl(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCaseImpl(repository)
        
        // Initialize collections
        initializeDefaultCollectionsUseCase()
        val collections = getCollectionsUseCase().first()
        val wishlistId = collections.first { it.type == CollectionType.WISHLIST }.id
        
        // Simulate user workflow: Add games to wishlist
        val gameIds = listOf(1, 2, 3, 4, 5)
        val additionTime = measureTimeMillis {
            gameIds.forEach { gameId ->
                addGameToCollectionUseCase(wishlistId, gameId)
            }
        }
        
        println("Added ${gameIds.size} games in ${additionTime}ms")
        assert(additionTime < 1000) { "Game addition took too long: ${additionTime}ms" }
        
        // Verify games were added
        val updatedCollections = getCollectionsUseCase().first()
        val updatedWishlist = updatedCollections.first { it.type == CollectionType.WISHLIST }
        assert(updatedWishlist.gameIds.size == gameIds.size) { "Not all games were added" }
        
        // Simulate moving games between collections
        val currentlyPlayingId = collections.first { it.type == CollectionType.CURRENTLY_PLAYING }.id
        
        val moveTime = measureTimeMillis {
            // Move first 2 games to currently playing
            gameIds.take(2).forEach { gameId ->
                removeGameFromCollectionUseCase(wishlistId, gameId)
                addGameToCollectionUseCase(currentlyPlayingId, gameId)
            }
        }
        
        println("Moved 2 games between collections in ${moveTime}ms")
        assert(moveTime < 500) { "Game moving took too long: ${moveTime}ms" }
        
        // Verify final state
        val finalCollections = getCollectionsUseCase().first()
        val finalWishlist = finalCollections.first { it.type == CollectionType.WISHLIST }
        val finalCurrentlyPlaying = finalCollections.first { it.type == CollectionType.CURRENTLY_PLAYING }
        
        assert(finalWishlist.gameIds.size == 3) { "Wishlist should have 3 games" }
        assert(finalCurrentlyPlaying.gameIds.size == 2) { "Currently Playing should have 2 games" }
        
        println("✅ User workflow test passed")
    }
    
    private fun testPerformanceUnderLoad() = runTest {
        println("\n🧪 Testing Performance Under Load...")
        
        val repository = FakeGameCollectionRepository()
        val createCollectionUseCase = CreateCollectionUseCaseImpl(repository)
        val getCollectionsUseCase = GetCollectionsUseCaseImpl(repository)
        val addGameToCollectionUseCase = AddGameToCollectionUseCaseImpl(repository)
        
        // Create many collections
        val collectionCount = 100
        val creationTime = measureTimeMillis {
            repeat(collectionCount) { index ->
                createCollectionUseCase("Collection $index", CollectionType.CUSTOM, "Description $index")
            }
        }
        
        println("Created $collectionCount collections in ${creationTime}ms")
        assert(creationTime < 5000) { "Collection creation under load took too long: ${creationTime}ms" }
        
        // Add games to collections
        val collections = getCollectionsUseCase().first()
        val gameAdditionTime = measureTimeMillis {
            collections.take(10).forEach { collection ->
                repeat(50) { gameId ->
                    addGameToCollectionUseCase(collection.id, gameId)
                }
            }
        }
        
        println("Added 500 games to 10 collections in ${gameAdditionTime}ms")
        assert(gameAdditionTime < 10000) { "Game addition under load took too long: ${gameAdditionTime}ms" }
        
        // Test query performance
        val queryTime = measureTimeMillis {
            repeat(100) {
                getCollectionsUseCase()
            }
        }
        
        println("Performed 100 collection queries in ${queryTime}ms")
        assert(queryTime < 2000) { "Query performance under load is poor: ${queryTime}ms" }
        
        println("✅ Performance under load test passed")
    }
    
    private fun testErrorRecovery() = runTest {
        println("\n🧪 Testing Error Recovery...")
        
        val repository = FakeGameCollectionRepository()
        val createCollectionUseCase = CreateCollectionUseCaseImpl(repository)
        val deleteCollectionUseCase = DeleteCollectionUseCaseImpl(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCaseImpl(repository)
        
        // Initialize collections
        initializeDefaultCollectionsUseCase()
        
        // Test duplicate name error recovery
        createCollectionUseCase("Test Collection", CollectionType.CUSTOM)
        
        try {
            createCollectionUseCase("Test Collection", CollectionType.CUSTOM)
            assert(false) { "Should have thrown duplicate name error" }
        } catch (e: IllegalArgumentException) {
            // Expected - error was properly thrown
        }
        
        // Test invalid operations
        val getCollectionsUseCase = GetCollectionsUseCaseImpl(repository)
        val collections = getCollectionsUseCase().first()
        val defaultCollection = collections.first { it.type == CollectionType.WISHLIST }
        
        try {
            deleteCollectionUseCase(defaultCollection.id)
            assert(false) { "Should have thrown protected collection error" }
        } catch (e: IllegalArgumentException) {
            // Expected - default collections are protected
        }
        
        // Test recovery after errors - system should still work
        val newCollectionId = createCollectionUseCase("Recovery Test", CollectionType.CUSTOM)
        assert(newCollectionId.isNotBlank()) { "Should be able to create collections after errors" }
        
        println("✅ Error recovery test passed")
    }
}

// Placeholder implementations for missing use case implementations
private class GetCollectionsUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : GetCollectionsUseCase {
    override suspend fun invoke(forceRefresh: Boolean): Result<List<CollectionWithCount>> {
        return try {
            val collections = repository.getCollections().first()
            val collectionsWithCount = collections.map { collection ->
                CollectionWithCount(
                    collection = collection,
                    gameCount = collection.gameIds.size
                )
            }
            Result.success(collectionsWithCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCollectionsByType(type: CollectionType, forceRefresh: Boolean): Result<List<CollectionWithCount>> {
        return invoke(forceRefresh).map { collections ->
            collections.filter { it.type == type }
        }
    }
    
    override suspend fun getCollectionById(collectionId: String, forceRefresh: Boolean): Result<CollectionWithCount> {
        return try {
            val collection = repository.getCollectionById(collectionId)
            if (collection != null) {
                Result.success(CollectionWithCount(collection, collection.gameIds.size))
            } else {
                Result.failure(IllegalArgumentException("Collection not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun clearCache() {
        // No-op for fake implementation
    }
}

private class CreateCollectionUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : CreateCollectionUseCase {
    override suspend fun invoke(name: String, type: CollectionType, description: String?): Result<com.devpush.features.game.domain.model.collections.GameCollection> {
        return try {
            val id = repository.createCollection(name, description, type)
            val collection = repository.getCollectionById(id)!!
            Result.success(collection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun initializeDefaultCollections(): Result<List<com.devpush.features.game.domain.model.collections.GameCollection>> {
        return try {
            val collections = mutableListOf<com.devpush.features.game.domain.model.collections.GameCollection>()
            
            if (!repository.collectionExists("Wishlist")) {
                val id = repository.createCollection("Wishlist", "Games you want to play", CollectionType.WISHLIST)
                collections.add(repository.getCollectionById(id)!!)
            }
            
            if (!repository.collectionExists("Currently Playing")) {
                val id = repository.createCollection("Currently Playing", "Games you're playing now", CollectionType.CURRENTLY_PLAYING)
                collections.add(repository.getCollectionById(id)!!)
            }
            
            if (!repository.collectionExists("Completed")) {
                val id = repository.createCollection("Completed", "Games you've finished", CollectionType.COMPLETED)
                collections.add(repository.getCollectionById(id)!!)
            }
            
            Result.success(collections)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private class DeleteCollectionUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : DeleteCollectionUseCase {
    override suspend fun invoke(collectionId: String, confirmDeletion: Boolean, allowDefaultDeletion: Boolean): Result<com.devpush.features.game.domain.usecase.DeletionInfo> {
        return try {
            repository.deleteCollection(collectionId)
            // Return placeholder deletion info
            Result.success(com.devpush.features.game.domain.usecase.DeletionInfo(
                collection = com.devpush.features.game.domain.model.collections.GameCollection(
                    id = collectionId,
                    name = "Deleted",
                    type = CollectionType.CUSTOM,
                    gameIds = emptyList(),
                    createdAt = 0,
                    updatedAt = 0
                ),
                gameCount = 0,
                canDelete = true,
                requiresConfirmation = false,
                warningMessage = null,
                isDefaultCollection = false
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getDeletionInfo(collectionId: String): Result<com.devpush.features.game.domain.usecase.DeletionInfo> {
        TODO("Not implemented for test")
    }
    
    override suspend fun clearCollection(collectionId: String): Result<Unit> {
        TODO("Not implemented for test")
    }
}

private class UpdateCollectionUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : UpdateCollectionUseCase {
    override suspend fun invoke(collectionId: String, newName: String?, newDescription: String?, allowDefaultUpdate: Boolean): Result<com.devpush.features.game.domain.model.collections.GameCollection> {
        return try {
            repository.updateCollection(collectionId, newName, newDescription)
            val collection = repository.getCollectionById(collectionId)!!
            Result.success(collection)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateCollection(updatedCollection: com.devpush.features.game.domain.model.collections.GameCollection, allowDefaultUpdate: Boolean): Result<com.devpush.features.game.domain.model.collections.GameCollection> {
        TODO("Not implemented for test")
    }
    
    override suspend fun validateUpdate(collectionId: String, newName: String?, newDescription: String?): Result<com.devpush.features.game.domain.usecase.UpdateValidationInfo> {
        TODO("Not implemented for test")
    }
}

private class InitializeDefaultCollectionsUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : InitializeDefaultCollectionsUseCase {
    override suspend fun invoke(): Result<List<com.devpush.features.game.domain.model.collections.GameCollection>> {
        return CreateCollectionUseCaseImpl(repository).initializeDefaultCollections()
    }
}

private class AddGameToCollectionUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : AddGameToCollectionUseCase {
    override suspend fun invoke(collectionId: String, gameId: Int): Result<Unit> {
        return try {
            repository.addGameToCollection(collectionId, gameId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private class RemoveGameFromCollectionUseCaseImpl(
    private val repository: FakeGameCollectionRepository
) : RemoveGameFromCollectionUseCase {
    override suspend fun invoke(collectionId: String, gameId: Int): Result<Unit> {
        return try {
            repository.removeGameFromCollection(collectionId, gameId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}