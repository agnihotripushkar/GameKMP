package com.devpush.kmp.collections

import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import com.devpush.features.collections.ui.collections.CollectionDetailViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Integration tests for CollectionDetailViewModel.
 * Tests the interaction between ViewModel and use cases for collection detail operations.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectionDetailViewModelIntegrationTest {

    private lateinit var repository: FakeGameCollectionRepository
    private lateinit var viewModel: CollectionDetailViewModel
    private lateinit var collectionId: String
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        
        repository = FakeGameCollectionRepository()
        
        // Initialize default collections and get wishlist ID
        val initializeUseCase = InitializeDefaultCollectionsUseCase(repository)
        initializeUseCase()
        
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val collections = getCollectionsUseCase().first()
        collectionId = collections.first { it.type == CollectionType.WISHLIST }.id
        
        val addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        val removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCase(repository)
        val updateCollectionUseCase = UpdateCollectionUseCase(repository)
        
        viewModel = CollectionDetailViewModel(
            collectionId = collectionId,
            getCollectionsUseCase = getCollectionsUseCase,
            addGameToCollectionUseCase = addGameToCollectionUseCase,
            removeGameFromCollectionUseCase = removeGameFromCollectionUseCase,
            updateCollectionUseCase = updateCollectionUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel loads collection details correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNotNull(state.collection)
        assertEquals(CollectionType.WISHLIST, state.collection!!.type)
        assertEquals("Wishlist", state.collection!!.name)
        assertEquals(0, state.collection!!.gameIds.size)
    }

    @Test
    fun `viewModel adds games to collection successfully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add games to collection
        viewModel.addGameToCollection(1)
        viewModel.addGameToCollection(2)
        viewModel.addGameToCollection(3)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.collection)
        assertEquals(3, state.collection!!.gameIds.size)
        assertTrue(state.collection!!.gameIds.contains(1))
        assertTrue(state.collection!!.gameIds.contains(2))
        assertTrue(state.collection!!.gameIds.contains(3))
    }

    @Test
    fun `viewModel removes games from collection successfully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // First add some games
        viewModel.addGameToCollection(1)
        viewModel.addGameToCollection(2)
        viewModel.addGameToCollection(3)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateAfterAdding = viewModel.uiState.first()
        assertEquals(3, stateAfterAdding.collection!!.gameIds.size)
        
        // Remove one game
        viewModel.removeGameFromCollection(2)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(2, finalState.collection!!.gameIds.size)
        assertTrue(finalState.collection!!.gameIds.contains(1))
        assertFalse(finalState.collection!!.gameIds.contains(2))
        assertTrue(finalState.collection!!.gameIds.contains(3))
    }

    @Test
    fun `viewModel updates collection details successfully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Update collection name and description
        viewModel.updateCollection("My Updated Wishlist", "Updated description")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.collection)
        assertEquals("My Updated Wishlist", state.collection!!.name)
        assertEquals("Updated description", state.collection!!.description)
    }

    @Test
    fun `viewModel handles duplicate game additions gracefully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add same game multiple times
        viewModel.addGameToCollection(1)
        viewModel.addGameToCollection(1)
        viewModel.addGameToCollection(1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.collection)
        assertEquals(1, state.collection!!.gameIds.size)
        assertTrue(state.collection!!.gameIds.contains(1))
    }

    @Test
    fun `viewModel handles removing non-existent games gracefully`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add one game
        viewModel.addGameToCollection(1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateAfterAdding = viewModel.uiState.first()
        assertEquals(1, stateAfterAdding.collection!!.gameIds.size)
        
        // Try to remove non-existent game
        viewModel.removeGameFromCollection(999)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(1, finalState.collection!!.gameIds.size) // Should remain unchanged
        assertTrue(finalState.collection!!.gameIds.contains(1))
    }

    @Test
    fun `viewModel handles update validation errors`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Try to update with empty name
        viewModel.updateCollection("", "Valid description")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("empty"))
        
        // Original collection should remain unchanged
        assertEquals("Wishlist", state.collection!!.name)
    }

    @Test
    fun `viewModel handles collection not found scenario`() = runTest {
        // Create ViewModel with non-existent collection ID
        val nonExistentId = "non-existent-id"
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val addGameToCollectionUseCase = AddGameToCollectionUseCase(repository)
        val removeGameFromCollectionUseCase = RemoveGameFromCollectionUseCase(repository)
        val updateCollectionUseCase = UpdateCollectionUseCase(repository)
        
        val invalidViewModel = CollectionDetailViewModel(
            collectionId = nonExistentId,
            getCollectionsUseCase = getCollectionsUseCase,
            addGameToCollectionUseCase = addGameToCollectionUseCase,
            removeGameFromCollectionUseCase = removeGameFromCollectionUseCase,
            updateCollectionUseCase = updateCollectionUseCase
        )
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = invalidViewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.collection)
        assertNotNull(state.error)
    }

    @Test
    fun `viewModel refreshes collection data correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add game directly to repository (simulating external change)
        repository.addGameToCollection(collectionId, 999)
        
        // Refresh the ViewModel
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.collection)
        assertEquals(1, state.collection!!.gameIds.size)
        assertTrue(state.collection!!.gameIds.contains(999))
    }

    @Test
    fun `viewModel clears errors correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Cause an error
        viewModel.updateCollection("", "Description")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateWithError = viewModel.uiState.first()
        assertNotNull(stateWithError.error)
        
        // Clear the error
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val clearedState = viewModel.uiState.first()
        assertNull(clearedState.error)
    }

    @Test
    fun `viewModel handles batch operations correctly`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add multiple games in sequence
        val gameIds = listOf(1, 2, 3, 4, 5)
        gameIds.forEach { gameId ->
            viewModel.addGameToCollection(gameId)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateAfterAdding = viewModel.uiState.first()
        assertEquals(5, stateAfterAdding.collection!!.gameIds.size)
        
        // Remove multiple games in sequence
        gameIds.take(3).forEach { gameId ->
            viewModel.removeGameFromCollection(gameId)
        }
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(2, finalState.collection!!.gameIds.size)
        assertTrue(finalState.collection!!.gameIds.contains(4))
        assertTrue(finalState.collection!!.gameIds.contains(5))
    }

    @Test
    fun `viewModel maintains collection type integrity`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Perform various operations
        viewModel.addGameToCollection(1)
        viewModel.updateCollection("Updated Name", "Updated Description")
        viewModel.removeGameFromCollection(1)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.collection)
        // Collection type should remain unchanged
        assertEquals(CollectionType.WISHLIST, state.collection!!.type)
        // But name and description should be updated
        assertEquals("Updated Name", state.collection!!.name)
        assertEquals("Updated Description", state.collection!!.description)
    }
}