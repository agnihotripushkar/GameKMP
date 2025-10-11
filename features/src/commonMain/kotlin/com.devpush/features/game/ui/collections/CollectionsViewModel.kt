package com.devpush.features.game.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.usecase.CreateCollectionUseCase
import com.devpush.features.game.domain.usecase.DeleteCollectionUseCase
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.CollectionWithCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the Collections screen
 */
data class CollectionsUiState(
    val collections: List<CollectionWithCount> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: CollectionError? = null,
    val editingCollection: GameCollection? = null,
    val showCreateDialog: Boolean = false,
    val showDeleteConfirmation: String? = null // Collection ID to delete
)

class CollectionsViewModel(
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState = _uiState.asStateFlow()
    
    private var loadCollectionsJob: Job? = null
    private var createCollectionJob: Job? = null
    private var deleteCollectionJob: Job? = null

    init {
        loadCollections()
    }

    /**
     * Loads collections from the repository
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     */
    fun loadCollections(forceRefresh: Boolean = false) {
        loadCollectionsJob?.cancel()
        loadCollectionsJob = flow {
            emit(getCollectionsUseCase(forceRefresh))
        }.flowOn(Dispatchers.IO)
            .catch { exception ->
                emit(Result.failure(exception))
            }
            .onStart {
                _uiState.update { 
                    it.copy(
                        isLoading = !forceRefresh, // Don't show loading spinner for refresh
                        isRefreshing = forceRefresh,
                        error = null
                    ) 
                }
            }
            .onEach { result ->
                result.onSuccess { collections ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            collections = collections,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to load collections: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error
                        ) 
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Refreshes collections by forcing a reload
     */
    fun refreshCollections() {
        loadCollections(forceRefresh = true)
    }

    /**
     * Creates a new collection
     * @param name The name of the collection
     * @param type The type of the collection
     */
    fun createCollection(name: String, type: CollectionType) {
        createCollectionJob?.cancel()
        createCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = createCollectionUseCase(name, type)
                
                result.onSuccess { collection ->
                    // Refresh collections to show the new one
                    loadCollections(forceRefresh = true)
                    
                    _uiState.update { 
                        it.copy(
                            showCreateDialog = false,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to create collection: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(error = error)
                    }
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to create collection: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(error = error)
                }
            }
        }
    }

    /**
     * Deletes a collection
     * @param collectionId The ID of the collection to delete
     */
    fun deleteCollection(collectionId: String) {
        deleteCollectionJob?.cancel()
        deleteCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = deleteCollectionUseCase(collectionId)
                
                result.onSuccess {
                    // Refresh collections to remove the deleted one
                    loadCollections(forceRefresh = true)
                    
                    _uiState.update { 
                        it.copy(
                            showDeleteConfirmation = null,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to delete collection: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(
                            showDeleteConfirmation = null,
                            error = error
                        )
                    }
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to delete collection: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(
                        showDeleteConfirmation = null,
                        error = error
                    )
                }
            }
        }
    }

    /**
     * Shows the create collection dialog
     */
    fun showCreateDialog() {
        _uiState.update { 
            it.copy(showCreateDialog = true)
        }
    }

    /**
     * Hides the create collection dialog
     */
    fun hideCreateDialog() {
        _uiState.update { 
            it.copy(showCreateDialog = false)
        }
    }

    /**
     * Shows delete confirmation for a collection
     * @param collectionId The ID of the collection to delete
     */
    fun showDeleteConfirmation(collectionId: String) {
        _uiState.update { 
            it.copy(showDeleteConfirmation = collectionId)
        }
    }

    /**
     * Hides the delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(showDeleteConfirmation = null)
        }
    }

    /**
     * Starts editing a collection
     * @param collection The collection to edit
     */
    fun startEditingCollection(collection: GameCollection) {
        _uiState.update { 
            it.copy(editingCollection = collection)
        }
    }

    /**
     * Stops editing a collection
     */
    fun stopEditingCollection() {
        _uiState.update { 
            it.copy(editingCollection = null)
        }
    }

    /**
     * Retries the last failed operation
     */
    fun retryOperation() {
        when {
            _uiState.value.error != null -> {
                clearError()
                loadCollections(forceRefresh = true)
            }
        }
    }

    /**
     * Clears the current error state
     */
    fun clearError() {
        _uiState.update { 
            it.copy(error = null)
        }
    }

    /**
     * Gets collections by type
     * @param type The collection type to filter by
     * @return List of collections of the specified type
     */
    fun getCollectionsByType(type: CollectionType): List<CollectionWithCount> {
        return _uiState.value.collections.filter { it.type == type }
    }

    /**
     * Gets default collections (wishlist, currently playing, completed)
     * @return List of default collections
     */
    fun getDefaultCollections(): List<CollectionWithCount> {
        return _uiState.value.collections.filter { it.type.isDefault }
    }

    /**
     * Gets custom collections
     * @return List of custom collections
     */
    fun getCustomCollections(): List<CollectionWithCount> {
        return _uiState.value.collections.filter { !it.type.isDefault }
    }

    /**
     * Checks if there are any collections
     * @return true if there are collections, false otherwise
     */
    fun hasCollections(): Boolean {
        return _uiState.value.collections.isNotEmpty()
    }

    /**
     * Gets the total number of collections
     * @return The total count of collections
     */
    fun getCollectionCount(): Int {
        return _uiState.value.collections.size
    }

    /**
     * Gets the total number of games across all collections
     * @return The total count of games in all collections
     */
    fun getTotalGameCount(): Int {
        return _uiState.value.collections.sumOf { it.gameCount }
    }

    override fun onCleared() {
        super.onCleared()
        loadCollectionsJob?.cancel()
        createCollectionJob?.cancel()
        deleteCollectionJob?.cancel()
    }
}