package com.devpush.features.game.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.game.domain.usecase.RemoveGameFromCollectionUseCase
import com.devpush.features.game.domain.usecase.UpdateCollectionUseCase
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.userRatingsReviews.domain.usecase.GetGamesWithUserDataUseCase
import com.devpush.features.userRatingsReviews.domain.usecase.SetUserRatingUseCase
import com.devpush.features.game.domain.model.collections.CollectionFilterState
import com.devpush.features.game.domain.model.collections.CollectionSortOption
import com.devpush.features.game.domain.usecase.FilterCollectionGamesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
 * UI state for the Collection Detail screen
 */
data class CollectionDetailUiState(
    val collection: GameCollection? = null,
    val games: List<Game> = emptyList(),
    val gamesWithUserData: List<GameWithUserData> = emptyList(),
    val filteredGamesWithUserData: List<GameWithUserData> = emptyList(),
    val availableGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingAvailableGames: Boolean = false,
    val isLoadingUserData: Boolean = false,
    val error: CollectionError? = null,
    val editingCollection: GameCollection? = null,
    val showAddGamesDialog: Boolean = false,
    val showRemoveConfirmation: Int? = null, // Game ID to remove
    val showReviewPreview: GameWithUserData? = null, // Game to show review preview for
    val filterState: CollectionFilterState = CollectionFilterState(),
    val showFilterPanel: Boolean = false
)

class CollectionDetailViewModel(
    private val collectionId: String,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val addGameToCollectionUseCase: AddGameToCollectionUseCase,
    private val removeGameFromCollectionUseCase: RemoveGameFromCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase,
    private val getGamesWithUserDataUseCase: GetGamesWithUserDataUseCase,
    private val setUserRatingUseCase: SetUserRatingUseCase,
    private val filterCollectionGamesUseCase: FilterCollectionGamesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    private var loadCollectionJob: Job? = null
    private var loadGamesJob: Job? = null
    private var loadAvailableGamesJob: Job? = null
    private var addGameJob: Job? = null
    private var removeGameJob: Job? = null
    private var updateCollectionJob: Job? = null
    
    init {
        loadCollection()
    }

    /**
     * Loads the collection and its games
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     */
    fun loadCollection(forceRefresh: Boolean = false) {
        loadCollectionJob?.cancel()
        loadCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { 
                    it.copy(
                        isLoading = !forceRefresh,
                        isRefreshing = forceRefresh,
                        error = null
                    ) 
                }
                
                // Get the specific collection by ID
                val result = getCollectionsUseCase.getCollectionById(collectionId, forceRefresh)
                
                result.onSuccess { collectionWithCount ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            collection = collectionWithCount.collection,
                            isLoading = false,
                            isRefreshing = false,
                            error = null
                        )
                    }
                    
                    // Load user data for games in the collection
                    loadUserDataForGames(collectionWithCount.collection.gameIds)
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.CollectionNotFound(collectionId)
                    }
                    
                    _uiState.update { currentState ->
                        currentState.copy(
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
                        "Failed to load collection: ${exception.message}",
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
     * Refreshes the current collection
     */
    fun refresh() {
        loadCollection(forceRefresh = true)
    }

    /**
     * Loads available games that can be added to the collection
     * For now, this is a placeholder - in a real implementation, this would
     * fetch games from a game repository and filter out already added games
     */
    fun loadAvailableGames() {
        // Placeholder implementation - in a real app, this would load from GameRepository
        _uiState.update { 
            it.copy(
                availableGames = emptyList(), // Placeholder
                isLoadingAvailableGames = false
            )
        }
    }

    /**
     * Adds a game to the collection
     * @param gameId The ID of the game to add
     */
    fun addGameToCollection(gameId: Int) {
        addGameJob?.cancel()
        addGameJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = addGameToCollectionUseCase(collectionId, gameId)
                
                result.onSuccess {
                    // Refresh collection to show new game
                    refresh()
                    
                    _uiState.update { 
                        it.copy(
                            showAddGamesDialog = false,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to add game: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(error = error)
                    }
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to add game: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(error = error)
                }
            }
        }
    }

    /**
     * Adds multiple games to the collection
     * @param gameIds List of game IDs to add
     */
    fun addGamesToCollection(gameIds: List<Int>) {
        addGameJob?.cancel()
        addGameJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Add games one by one
                for (gameId in gameIds) {
                    addGameToCollectionUseCase(collectionId, gameId)
                }
                
                // Refresh collection to show new games
                refresh()
                
                _uiState.update { 
                    it.copy(
                        showAddGamesDialog = false,
                        error = null
                    )
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to add games: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(error = error)
                }
            }
        }
    }

    /**
     * Removes a game from the collection
     * @param gameId The ID of the game to remove
     */
    fun removeGameFromCollection(gameId: Int) {
        removeGameJob?.cancel()
        removeGameJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = removeGameFromCollectionUseCase(collectionId, gameId)
                
                result.onSuccess {
                    // Refresh collection to show updated games
                    refresh()
                    
                    _uiState.update { 
                        it.copy(
                            showRemoveConfirmation = null,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to remove game: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(
                            showRemoveConfirmation = null,
                            error = error
                        )
                    }
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to remove game: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(
                        showRemoveConfirmation = null,
                        error = error
                    )
                }
            }
        }
    }

    /**
     * Updates the collection details
     * @param newName The new name for the collection
     * @param newDescription The new description for the collection
     */
    fun updateCollection(newName: String, newDescription: String?) {
        updateCollectionJob?.cancel()
        updateCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = updateCollectionUseCase(
                    collectionId = collectionId,
                    newName = newName,
                    newDescription = newDescription,
                    allowDefaultUpdate = false
                )
                
                result.onSuccess { collection ->
                    _uiState.update { 
                        it.copy(
                            collection = collection,
                            editingCollection = null,
                            error = null
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to update collection: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(error = error)
                    }
                }
            } catch (exception: Exception) {
                val error = CollectionError.UnknownError(
                    "Failed to update collection: ${exception.message}",
                    exception
                )
                
                _uiState.update { 
                    it.copy(error = error)
                }
            }
        }
    }

    /**
     * Shows the add games dialog
     */
    fun showAddGamesDialog() {
        loadAvailableGames()
        _uiState.update { 
            it.copy(showAddGamesDialog = true)
        }
    }

    /**
     * Hides the add games dialog
     */
    fun hideAddGamesDialog() {
        _uiState.update { 
            it.copy(showAddGamesDialog = false)
        }
    }

    /**
     * Shows remove confirmation for a game
     * @param gameId The ID of the game to remove
     */
    fun showRemoveConfirmation(gameId: Int) {
        _uiState.update { 
            it.copy(showRemoveConfirmation = gameId)
        }
    }

    /**
     * Hides the remove confirmation dialog
     */
    fun hideRemoveConfirmation() {
        _uiState.update { 
            it.copy(showRemoveConfirmation = null)
        }
    }

    /**
     * Starts editing the collection
     */
    fun startEditingCollection() {
        _uiState.update { 
            it.copy(editingCollection = it.collection)
        }
    }

    /**
     * Stops editing the collection
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
                loadCollection(forceRefresh = true)
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
     * Checks if the collection is empty
     * @return true if the collection has no games, false otherwise
     */
    fun isCollectionEmpty(): Boolean {
        return _uiState.value.collection?.gameIds?.isEmpty() == true
    }

    /**
     * Gets the number of games in the collection
     * @return The count of games in the collection
     */
    fun getGameCount(): Int {
        return _uiState.value.collection?.gameIds?.size ?: 0
    }

    /**
     * Checks if a game is in the collection
     * @param gameId The ID of the game to check
     * @return true if the game is in the collection, false otherwise
     */
    fun isGameInCollection(gameId: Int): Boolean {
        return _uiState.value.collection?.gameIds?.contains(gameId) == true
    }
    
    /**
     * Loads user data (ratings and reviews) for the given games
     * @param gameIds List of game IDs to load user data for
     */
    private fun loadUserDataForGames(gameIds: List<Int>) {
        if (gameIds.isEmpty()) {
            _uiState.update { it.copy(gamesWithUserData = emptyList()) }
            return
        }
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoadingUserData = true) }
                
                val result = getGamesWithUserDataUseCase(gameIds)
                
                result.onSuccess { gamesWithUserData ->
                    _uiState.update { currentState ->
                        val filteredGames = filterCollectionGamesUseCase(gamesWithUserData, currentState.filterState)
                        currentState.copy(
                            gamesWithUserData = gamesWithUserData,
                            filteredGamesWithUserData = filteredGames,
                            isLoadingUserData = false
                        )
                    }
                }.onFailure { exception ->
                    // Don't show error for user data loading failure, just log it
                    // The collection should still work without user data
                    _uiState.update { it.copy(isLoadingUserData = false) }
                }
            } catch (exception: Exception) {
                _uiState.update { it.copy(isLoadingUserData = false) }
            }
        }
    }
    
    /**
     * Sets a quick rating for a game
     * @param gameId The ID of the game to rate
     * @param rating The rating (1-5 stars)
     */
    fun setQuickRating(gameId: Int, rating: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = setUserRatingUseCase(gameId, rating)
                
                result.onSuccess {
                    // Reload user data to reflect the new rating
                    _uiState.value.collection?.gameIds?.let { gameIds ->
                        loadUserDataForGames(gameIds)
                    }
                }.onFailure { exception ->
                    // Handle rating error - could show a snackbar or toast
                    // For now, just ignore the error
                }
            } catch (exception: Exception) {
                // Handle exception
            }
        }
    }
    
    /**
     * Shows the review preview dialog for a game
     * @param gameWithUserData The game with user data to show review for
     */
    fun showReviewPreview(gameWithUserData: GameWithUserData) {
        _uiState.update { 
            it.copy(showReviewPreview = gameWithUserData)
        }
    }
    
    /**
     * Hides the review preview dialog
     */
    fun hideReviewPreview() {
        _uiState.update { 
            it.copy(showReviewPreview = null)
        }
    }
    
    /**
     * Updates the filter state and applies filtering
     */
    fun updateFilterState(newFilterState: CollectionFilterState) {
        _uiState.update { currentState ->
            val filteredGames = filterCollectionGamesUseCase(currentState.gamesWithUserData, newFilterState)
            currentState.copy(
                filterState = newFilterState,
                filteredGamesWithUserData = filteredGames
            )
        }
    }
    
    /**
     * Updates the search query
     */
    fun updateSearchQuery(query: String) {
        val newFilterState = _uiState.value.filterState.copy(searchQuery = query)
        updateFilterState(newFilterState)
    }
    
    /**
     * Updates the sort option
     */
    fun updateSortOption(sortOption: CollectionSortOption) {
        val newFilterState = _uiState.value.filterState.copy(sortBy = sortOption)
        updateFilterState(newFilterState)
    }
    
    /**
     * Sets the user rating filter range
     */
    fun setUserRatingFilter(minRating: Int, maxRating: Int) {
        val newFilterState = _uiState.value.filterState.setUserRatingRange(minRating, maxRating)
        updateFilterState(newFilterState)
    }
    
    /**
     * Toggles the "show only rated" filter
     */
    fun toggleShowOnlyRated() {
        val newFilterState = _uiState.value.filterState.toggleShowOnlyRated()
        updateFilterState(newFilterState)
    }
    
    /**
     * Toggles the "show only reviewed" filter
     */
    fun toggleShowOnlyReviewed() {
        val newFilterState = _uiState.value.filterState.toggleShowOnlyReviewed()
        updateFilterState(newFilterState)
    }
    
    /**
     * Clears all filters
     */
    fun clearAllFilters() {
        updateFilterState(CollectionFilterState())
    }
    
    /**
     * Shows the filter panel
     */
    fun showFilterPanel() {
        _uiState.update { it.copy(showFilterPanel = true) }
    }
    
    /**
     * Hides the filter panel
     */
    fun hideFilterPanel() {
        _uiState.update { it.copy(showFilterPanel = false) }
    }

    override fun onCleared() {
        super.onCleared()
        loadCollectionJob?.cancel()
        loadGamesJob?.cancel()
        loadAvailableGamesJob?.cancel()
        addGameJob?.cancel()
        removeGameJob?.cancel()
        updateCollectionJob?.cancel()
    }
}