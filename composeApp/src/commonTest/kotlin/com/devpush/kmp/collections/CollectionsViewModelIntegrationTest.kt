package com.devpush.kmp.collections

import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.game.domain.usecase.*
import com.devpush.features.bookmarklist.ui.collections.CollectionsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import kotlin.test.*

/**
 * Integration tests for CollectionsViewModel.
 * Tests the interaction between ViewModel and use cases.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CollectionsViewModelIntegrationTest {

    private lateinit var repository: FakeGameCollectionRepository
    private lateinit var viewModel: CollectionsViewModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        repository = FakeGameCollectionRepository()
        
        val getCollectionsUseCase = GetCollectionsUseCase(repository)
        val createCollectionUseCase = CreateCollectionUseCase(repository)
        val deleteCollectionUseCase = DeleteCollectionUseCase(repository)
        val initializeDefaultCollectionsUseCase = InitializeDefaultCollectionsUseCase(repository)
        
        viewModel = CollectionsViewModel(
            getCollectionsUseCase = getCollectionsUseCase,
            createCollectionUseCase = createCollectionUseCase,
            deleteCollectionUseCase = deleteCollectionUseCase,
            initializeDefaultCollectionsUseCase = initializeDefaultCollectionsUseCase
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `viewModel initializes with default collections`() = runTest {
        // Wait for initialization to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals(3, state.collections.size)
        
        val collectionTypes = state.collections.map { it.type }
        assertTrue(collectionTypes.contains(CollectionType.WISHLIST))
        assertTrue(collectionTypes.contains(CollectionType.CURRENTLY_PLAYING))
        assertTrue(collectionTypes.contains(CollectionType.COMPLETED))
    }

    @Test
    fun `viewModel creates custom collection successfully`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create custom collection
        viewModel.createCollection("My Games", "Personal collection")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(4, state.collections.size)
        
        val customCollection = state.collections.first { it.type == CollectionType.CUSTOM }
        assertEquals("My Games", customCollection.name)
        assertEquals("Personal collection", customCollection.description)
    }

    @Test
    fun `viewModel handles collection creation errors`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Try to create collection with empty name
        viewModel.createCollection("", "Description")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("empty"))
        
        // Clear error
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val clearedState = viewModel.uiState.first()
        assertNull(clearedState.error)
    }

    @Test
    fun `viewModel deletes custom collection successfully`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create custom collection
        viewModel.createCollection("To Delete", "Will be deleted")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateAfterCreation = viewModel.uiState.first()
        assertEquals(4, stateAfterCreation.collections.size)
        
        val customCollection = stateAfterCreation.collections.first { it.type == CollectionType.CUSTOM }
        
        // Delete the custom collection
        viewModel.deleteCollection(customCollection.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(3, finalState.collections.size)
        assertFalse(finalState.collections.any { it.type == CollectionType.CUSTOM })
    }

    @Test
    fun `viewModel prevents deletion of default collections`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        val wishlist = state.collections.first { it.type == CollectionType.WISHLIST }
        
        // Try to delete default collection
        viewModel.deleteCollection(wishlist.id)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertNotNull(finalState.error)
        assertTrue(finalState.error!!.contains("default"))
        assertEquals(3, finalState.collections.size) // Should remain unchanged
    }

    @Test
    fun `viewModel handles refresh correctly`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Add a collection directly to repository (simulating external change)
        repository.createCollection("External Collection", "Added externally", CollectionType.CUSTOM)
        
        // Refresh
        viewModel.refresh()
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(4, state.collections.size)
        assertTrue(state.collections.any { it.name == "External Collection" })
    }

    @Test
    fun `viewModel maintains loading states correctly`() = runTest {
        // Check initial loading state
        val initialState = viewModel.uiState.first()
        assertTrue(initialState.isLoading)
        
        // Wait for initialization to complete
        testDispatcher.scheduler.advanceUntilIdle()
        
        val loadedState = viewModel.uiState.first()
        assertFalse(loadedState.isLoading)
        
        // Test loading during collection creation
        viewModel.createCollection("Test Collection", "Test")
        
        // The loading state might be brief, so we check the final state
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertEquals(4, finalState.collections.size)
    }

    @Test
    fun `viewModel handles duplicate collection names`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create first collection
        viewModel.createCollection("Duplicate Name", "First")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val stateAfterFirst = viewModel.uiState.first()
        assertEquals(4, stateAfterFirst.collections.size)
        assertNull(stateAfterFirst.error)
        
        // Try to create second collection with same name
        viewModel.createCollection("Duplicate Name", "Second")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val finalState = viewModel.uiState.first()
        assertEquals(4, finalState.collections.size) // Should not increase
        assertNotNull(finalState.error)
        assertTrue(finalState.error!!.contains("already exists"))
    }

    @Test
    fun `viewModel sorts collections correctly`() = runTest {
        // Wait for initialization
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Create multiple custom collections
        viewModel.createCollection("Z Collection", "Last alphabetically")
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.createCollection("A Collection", "First alphabetically")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val state = viewModel.uiState.first()
        assertEquals(5, state.collections.size)
        
        // Check that default collections come first
        assertEquals(CollectionType.WISHLIST, state.collections[0].type)
        assertEquals(CollectionType.CURRENTLY_PLAYING, state.collections[1].type)
        assertEquals(CollectionType.COMPLETED, state.collections[2].type)
        
        // Custom collections should be sorted by creation time (first created first)
        val customCollections = state.collections.drop(3)
        assertEquals("Z Collection", customCollections[0].name) // Created first
        assertEquals("A Collection", customCollections[1].name) // Created second
    }
}