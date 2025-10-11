package com.devpush.features.game.ui.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.repository.GameRepository
import com.devpush.features.game.domain.repository.GameCollectionRepository
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.game.domain.usecase.RemoveGameFromCollectionUseCase
import com.devpush.features.game.domain.usecase.UpdateCollectionUseCase
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
    val availableGames: List<Game> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingAvailableGames: Boolean = false,
    val error: CollectionError? = null,
    val editingCollection: GameCollection? = null,
    val showAddGamesDialog: Boolean = false,
    val showRemoveConfirmation: Int? = null // Game ID to remove
)

class CollectionDetailViewModel(
    private val gameCollectionRepository: GameCollectionRepository,
    private val gameRepository: GameRepository,
    private val addGameToCollectionUseCase: AddGameToCollectionUseCase,
    private val removeGameFromCollectionUseCase: RemoveGameFromCollectionUseCase,
    private val updateCollectionUseCase: UpdateCollectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CollectionDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    private var loadCollectionJob: Job? = null
    private var loadGamesJob: Job? = null
    private var loadAvailableGamesJob: Job? = null
    private var addGameJob: Job? = null
    private var removeGameJob: Job? = null
    private var updateCollectionJob: Job? = null
    
    private var currentCollectionId: String? = null

    /**
     * Loads a collection and its games
     * @param collectionId The ID of the collection to load
     * @param forceRefresh Whether to bypass cache and fetch fresh data
     */
    fun loadCollection(collectionId: String, forceRefresh: Boolean = false) {
        if (currentCollectionId == collectionId && !forceRefresh) {
            return // Already loaded
        }
        
        currentCollectionId = collectionId
        loadCollectionJob?.cancel()
        loadCollectionJob = flow {
            // Load collection details and games in parallel
            val collectionDeferred = async { gameCollectionRepository.getCollectionById(collectionId) }
            val gamesDeferred = async { gameCollectionRepository.getGamesInCollection(collectionId) }
            
            val results = awaitAll(collectionDeferred, gamesDeferred)
            emit(Pair(results[0] as Result<GameCollection>, results[1] as Result<List<Game>>))
        }.flowOn(Dispatchers.IO)
            .catch { exception ->
                emit(Pair(Result.failure(exception), Result.failure(exception)))
            }
            .onStart {
                _uiState.update { 
                    it.copy(
                        isLoading = !forceRefresh,
                        isRefreshing = forceRefresh,
                        error = null
                    ) 
                }
            }
            .onEach { (collectionResult, gamesResult) ->
                var error: CollectionError? = null
                var collection: GameCollection? = null
                var games: List<Game> = emptyList()
                
                collectionResult.onSuccess { loadedCollection ->
                    collection = loadedCollection
                }.onFailure { exception ->
                    error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.CollectionNotFound(collectionId)
                    }
                }
                
                if (collection != null) {
                    gamesResult.onSuccess { loadedGames ->
                        games = loadedGames
                    }.onFailure { exception ->
                        error = when (exception) {
                            is CollectionError -> exception
                            else -> CollectionError.UnknownError(
                                "Failed to load games: ${exception.message}",
                                exception
                            )
                        }
                    }
                }
                
                _uiState.update { currentState ->
                    currentState.copy(
                        collection = collection,
                        games = games,
                        isLoading = false,
                        isRefreshing = false,
                        error = error
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Refreshes the current collection
     */
    fun refreshCollection() {
        currentCollectionId?.let { collectionId ->
            loadCollection(collectionId, forceRefresh = true)
        }
    }

    /**
     * Loads available games that can be added to the collection
     */
    fun loadAvailableGames() {
        loadAvailableGamesJob?.cancel()
        loadAvailableGamesJob = flow {
            emit(gameRepository.getGames())
        }.flowOn(Dispatchers.IO)
            .catch { exception ->
                emit(Result.failure(exception))
            }
            .onStart {
                _uiState.update { 
                    it.copy(
                        isLoadingAvailableGames = true,
                        error = null
                    ) 
                }
            }
            .onEach { result ->
                result.onSuccess { allGames ->
                    val currentCollection = _uiState.value.collection
                    val availableGames = if (currentCollection != null) {
                        // Filter out games already in the collection
                        allGames.filter { game ->
                            !currentCollection.containsGame(game.id)
                        }
                    } else {
                        allGames
                    }
                    
                    _uiState.update { 
                        it.copy(
                            availableGames = availableGames,
                            isLoadingAvailableGames = false
                        )
                    }
                }.onFailure { exception ->
                    val error = when (exception) {
                        is CollectionError -> exception
                        else -> CollectionError.UnknownError(
                            "Failed to load available games: ${exception.message}",
                            exception
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoadingAvailableGames = false,
                            error = error
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Adds games to the collection
     * @param gameIds List of game IDs to add
     */
    fun addGamesToCollection(gameIds: List<Int>) {
        val collectionId = _uiState.value.collection?.id ?: return
        
        addGameJob?.cancel()
        addGameJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Add games one by one (could be optimized with batch operation)
                val results = gameIds.map { gameId ->
                    async { addGameToCollectionUseCase(collectionId, gameId) }
                }.awaitAll()
                
                // Check if all operations succeeded
                val failures = results.filter { it.isFailure }
                if (failures.isNotEmpty()) {
                    val error = failures.first().exceptionOrNull()
                    val collectionError = when (error) {
                        is CollectionError -> error
                        else -> CollectionError.UnknownError(
                            "Failed to add some games: ${error?.message}",
                            error
                        )
                    }
                    
                    _uiState.update { 
                        it.copy(error = collectionError)
                    }
                } else {
                    // Refresh collection to show new games
                    refreshCollection()
                    
                    _uiState.update { 
                        it.copy(
                            showAddGamesDialog = false,
                            error = null
                        )
                    }
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
        val collectionId = _uiState.value.collection?.id ?: return
        
        removeGameJob?.cancel()
        removeGameJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = removeGameFromCollectionUseCase(collectionId, gameId)
                
                result.onSuccess {
                    // Refresh collection to show updated games
                    refreshCollection()
                    
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
     * @param updatedCollection The updated collection
     */
    fun updateCollection(updatedCollection: GameCollection) {
        updateCollectionJob?.cancel()
        updateCollectionJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = updateCollectionUseCase(updatedCollection)
                
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
                currentCollectionId?.let { collectionId ->
                    loadCollection(collectionId, forceRefresh = true)
                }
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
     * Gets games filtered by search query
     * @param query The search query
     * @return List of games matching the query
     */
    fun getFilteredGames(query: String): List<Game> {
        return if (query.isBlank()) {
            _uiState.value.games
        } else {
            _uiState.value.games.filter { game ->
                game.name.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Gets available games filtered by search query
     * @param query The search query
     * @return List of available games matching the query
     */
    fun getFilteredAvailableGames(query: String): List<Game> {
        return if (query.isBlank()) {
            _uiState.value.availableGames
        } else {
            _uiState.value.availableGames.filter { game ->
                game.name.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * Checks if the collection is empty
     * @return true if the collection has no games, false otherwise
     */
    fun isCollectionEmpty(): Boolean {
        return _uiState.value.games.isEmpty()
    }

    /**
     * Gets the number of games in the collection
     * @return The count of games in the collection
     */
    fun getGameCount(): Int {
        return _uiState.value.games.size
    }

    /**
     * Checks if a game is in the collection
     * @param gameId The ID of the game to check
     * @return true if the game is in the collection, false otherwise
     */
    fun isGameInCollection(gameId: Int): Boolean {
        return _uiState.value.collection?.containsGame(gameId) == true
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