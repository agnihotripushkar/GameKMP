package com.devpush.features.bookmarklist.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.bookmarklist.domain.collections.CollectionError
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import com.devpush.features.game.domain.usecase.CreateCollectionUseCase
import com.devpush.features.game.domain.usecase.DeleteCollectionUseCase
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.UpdateCollectionUseCase
import com.devpush.features.game.domain.usecase.InitializeDefaultCollectionsUseCase
import com.devpush.features.game.domain.usecase.CollectionWithCount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val collectionToDelete: GameCollection? = null, // Collection to delete
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val isUpdating: Boolean = false,
    val updateError: String? = null
)

class CollectionsViewModel(
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val createCollectionUseCase: CreateCollectionUseCase,
    private val deleteCollectionUseCase: DeleteCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val initializeDefaultCollectionsUseCase: InitializeDefaultCollectionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionsUiState())
    val uiState = _uiState.asStateFlow()
    
    private var loadCollectionsJob: Job? = null
    private var createCollectionJob: Job? = null
    private var deleteCollectionJob: Job? = null
    private var updateCollectionJob: Job? = null

    init {
        initializeAndLoadCollections()
    }

    /**
     * Initializes default collections and loads all collections
     */
    private fun initializeAndLoadCollections() {
        loadCollectionsJob?.cancel()
        loadCollectionsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isLoading = true,
                        error = null
                    ) 
                }
                
                // First initialize default collections
                initializeDefaultCollectionsUseCase()
                
                // Then load all collections
                loadCollections(forceRefresh = true)
                
            } catch (exception: Exception) {
                val error = when (exception) {
                    is CollectionError -> exception
                    else -> CollectionError.UnknownError(
                        "Failed to initialize collections: ${exception.message}",
                        exception
                    )
                }
                
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = error
                    ) 
                }
            }
        }
    }

    /**
     * Loads collections from the repository
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     */
    fun loadCollections(forceRefresh: Boolean = false) {
        loadCollectionsJob?.cancel()
        loadCollectionsJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isLoading = !forceRefresh, // Don't show loading spinner for refresh
                        isRefreshing = forceRefresh,
                        error = null
                    ) 
                }
                
                val result = getCollectionsUseCase(forceRefresh)
                
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
            } catch (exception: Exception) {
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
     * @param description Optional description for the collection
     */
    fun createCollection(name: String, description: String? = null) {
        createCollectionJob?.cancel()
        createCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = createCollectionUseCase(name, CollectionType.CUSTOM, description)
                
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
     * Shows delete confirmation for a collection
     * @param collection The collection to delete
     */
    fun showDeleteConfirmation(collection: GameCollection) {
        _uiState.update { 
            it.copy(
                collectionToDelete = collection,
                deleteError = null
            )
        }
    }

    /**
     * Hides the delete confirmation dialog
     */
    fun hideDeleteConfirmation() {
        _uiState.update { 
            it.copy(
                collectionToDelete = null,
                deleteError = null
            )
        }
    }

    /**
     * Deletes a collection by ID
     * @param collectionId The ID of the collection to delete
     */
    fun deleteCollection(collectionId: String) {
        deleteCollectionJob?.cancel()
        deleteCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isDeleting = true,
                        deleteError = null
                    )
                }
                
                val result = deleteCollectionUseCase(
                    collectionId = collectionId,
                    confirmDeletion = true,
                    allowDefaultDeletion = false
                )
                
                result.onSuccess { deletionInfo ->
                    // Refresh collections to remove the deleted one
                    loadCollections(forceRefresh = true)
                    
                    _uiState.update { 
                        it.copy(
                            isDeleting = false,
                            deleteError = null
                        )
                    }
                }.onFailure { exception ->
                    val errorMessage = when (exception) {
                        is CollectionError.DefaultCollectionProtected -> "Default collections cannot be deleted"
                        is CollectionError.CollectionNotFound -> "Collection not found"
                        is CollectionError.DatabaseError -> "Database error occurred. Please try again."
                        else -> "Failed to delete collection: ${exception.message}"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isDeleting = false,
                            deleteError = errorMessage
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isDeleting = false,
                        deleteError = "Failed to delete collection: ${exception.message}"
                    )
                }
            }
        }
    }

    /**
     * Deletes the currently selected collection
     */
    fun confirmDeleteCollection() {
        val collection = _uiState.value.collectionToDelete ?: return
        
        deleteCollectionJob?.cancel()
        deleteCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isDeleting = true,
                        deleteError = null
                    )
                }
                
                val result = deleteCollectionUseCase(
                    collectionId = collection.id,
                    confirmDeletion = true,
                    allowDefaultDeletion = false
                )
                
                result.onSuccess { deletionInfo ->
                    // Refresh collections to remove the deleted one
                    loadCollections(forceRefresh = true)
                    
                    _uiState.update { 
                        it.copy(
                            isDeleting = false,
                            collectionToDelete = null,
                            deleteError = null
                        )
                    }
                }.onFailure { exception ->
                    val errorMessage = when (exception) {
                        is CollectionError.DefaultCollectionProtected -> "Default collections cannot be deleted"
                        is CollectionError.CollectionNotFound -> "Collection not found"
                        is CollectionError.DatabaseError -> "Database error occurred. Please try again."
                        else -> "Failed to delete collection: ${exception.message}"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isDeleting = false,
                            deleteError = errorMessage
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isDeleting = false,
                        deleteError = "Failed to delete collection: ${exception.message}"
                    )
                }
            }
        }
    }

    /**
     * Clears the delete error
     */
    fun clearDeleteError() {
        _uiState.update { 
            it.copy(deleteError = null)
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
            it.copy(
                editingCollection = null,
                updateError = null
            )
        }
    }

    /**
     * Updates a collection's name and description
     * @param collectionId The ID of the collection to update
     * @param newName The new name for the collection
     * @param newDescription The new description for the collection (null for no description)
     */
    fun updateCollection(collectionId: String, newName: String, newDescription: String?) {
        updateCollectionJob?.cancel()
        updateCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isUpdating = true,
                        updateError = null
                    )
                }
                
                val result = updateCollectionUseCase(
                    collectionId = collectionId,
                    newName = newName,
                    newDescription = newDescription,
                    allowDefaultUpdate = false // Don't allow editing default collections by default
                )
                
                result.onSuccess { updatedCollection ->
                    // Refresh collections to show the updated one
                    loadCollections(forceRefresh = true)
                    
                    _uiState.update { 
                        it.copy(
                            isUpdating = false,
                            editingCollection = null,
                            updateError = null
                        )
                    }
                }.onFailure { exception ->
                    val errorMessage = when (exception) {
                        is CollectionError.ValidationError -> exception.reason
                        is CollectionError.CollectionNameExists -> "A collection with this name already exists"
                        is CollectionError.DefaultCollectionProtected -> "Cannot modify this default collection"
                        is CollectionError.CollectionNotFound -> "Collection not found"
                        is CollectionError.DatabaseError -> "Database error occurred. Please try again."
                        else -> "Failed to update collection: ${exception.message}"
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isUpdating = false,
                            updateError = errorMessage
                        )
                    }
                }
            } catch (exception: Exception) {
                _uiState.update { 
                    it.copy(
                        isUpdating = false,
                        updateError = "Failed to update collection: ${exception.message}"
                    )
                }
            }
        }
    }

    /**
     * Clears the update error
     */
    fun clearUpdateError() {
        _uiState.update { 
            it.copy(updateError = null)
        }
    }

    /**
     * Shares a collection (placeholder for future implementation)
     * @param collection The collection to share
     */
    fun shareCollection(collection: GameCollection) {
        // TODO: Implement sharing functionality
        // This could generate a shareable link, export collection data, etc.
        // For now, we'll just show a message that sharing is not yet implemented
        
        val shareText = buildString {
            append("Check out my ${collection.name} collection")
            if (collection.getGameCount() > 0) {
                append(" with ${collection.getGameCount()} games")
            }
            if (!collection.description.isNullOrBlank()) {
                append(": ${collection.description}")
            }
        }
        
        // In a real implementation, this would use platform-specific sharing APIs
        // For now, we could show a snackbar or copy to clipboard
        println("Sharing collection: $shareText")
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
        updateCollectionJob?.cancel()
    }
}