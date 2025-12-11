package com.devpush.features.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.repository.GameRepository
import com.devpush.features.game.domain.usecase.SearchGamesUseCase
import com.devpush.features.game.domain.usecase.FilterGamesUseCase
import com.devpush.features.game.domain.usecase.GetAvailableFiltersUseCase
import com.devpush.features.game.domain.usecase.GetCollectionsUseCase
import com.devpush.features.game.domain.usecase.AddGameToCollectionUseCase
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.SearchFilterError
import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.game.domain.validation.FilterValidator
import com.devpush.features.game.domain.validation.ValidationResult
import com.devpush.features.common.utils.SearchUtils
import com.devpush.features.common.utils.StringUtils
import io.github.aakira.napier.Napier
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



class GameViewModel(
    private val gameRepository: GameRepository,
    private val searchGamesUseCase: SearchGamesUseCase,
    private val filterGamesUseCase: FilterGamesUseCase,
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase,
    private val getCollectionsUseCase: GetCollectionsUseCase,
    private val addGameToCollectionUseCase: AddGameToCollectionUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState = _uiState.asStateFlow()
    
    private var searchJob: Job? = null
    private var filterJob: Job? = null
    
    // Cache for search results to improve performance
    private val searchCache = mutableMapOf<String, List<Game>>()
    private val filterCache = mutableMapOf<String, List<Game>>()
    
    // Performance optimization: limit cache size
    private val maxCacheSize = 50

    init {
        getGames()
        loadCollections()
    }

    fun getGames() {
        // Clear caches when refreshing data
        clearCaches()
        
        Napier.d("Extensions: getGames called", tag = "GameViewModel")
        flow {
            emit(gameRepository.getGames())
        }.flowOn(Dispatchers.IO)
            .catch { exception ->
                Napier.e("Error fetching games flow", exception, tag = "GameViewModel")
                emit(Result.failure(exception))
            }
            .onStart {
                _uiState.update { 
                    it.copy(
                        isLoading = true, 
                        error = null, 
                        filterError = null,
                        canRetry = false
                    ) 
                }
            }
            .onEach { result ->
                result.onSuccess { games ->
                    Napier.d("GameViewModel received success. Games count: ${games.size}", tag = "GameViewModel")
                    try {
                        val filterOptions = getAvailableFiltersUseCase(games)
                        _uiState.update { currentState ->
                            currentState.copy(
                                games = games,
                                filteredGames = games,
                                availablePlatforms = filterOptions.platforms,
                                availableGenres = filterOptions.genres,
                                isLoading = false,
                                error = null,
                                filterError = null,
                                canRetry = false
                            )
                        }
                    } catch (exception: Exception) {
                        Napier.e("Error updating UI state with games", exception, tag = "GameViewModel")
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                filterError = SearchFilterError.FilterLoadError,
                                canRetry = true
                            )
                        }
                    }
                }.onFailure { exception ->
                    Napier.e("GameViewModel received failure", exception, tag = "GameViewModel")
                    val errorType = when {
                        exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("network") } } == true -> 
                            SearchFilterError.NetworkError
                        exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("database") } } == true -> 
                            SearchFilterError.DatabaseError
                        else -> SearchFilterError.UnknownError(exception.message ?: "Unknown error occurred")
                    }
                    
                    _uiState.update { 
                        it.copy(
                            isLoading = false, 
                            error = errorType.message,
                            canRetry = true
                        ) 
                    }
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun refreshGames() {
        _uiState.update { it.copy(isRefreshing = true) }
        getGames()
    }
    
    fun retryOperation() {
        when {
            _uiState.value.error != null -> getGames()
            _uiState.value.filterError != null -> {
                // Retry filter loading
                _uiState.update { it.copy(filterError = null) }
                applySearchAndFilters()
            }
        }
    }
    
    fun clearError() {
        _uiState.update { 
            it.copy(
                error = null, 
                filterError = null,
                canRetry = false
            ) 
        }
    }
    
    fun updateSearchQuery(query: String) {
        // Validate search query before updating state
        val validationResult = FilterValidator.validateSearchQuery(query)
        
        if (validationResult.hasError) {
            val error = (validationResult as ValidationResult.Error).error
            _uiState.update { 
                it.copy(
                    filterError = error,
                    canRetry = false
                )
            }
            return
        }
        
        _uiState.update { 
            it.copy(
                searchFilterState = it.searchFilterState.copy(searchQuery = query),
                filterError = null // Clear any previous validation errors
            )
        }
        
        // Debounce search with 300ms delay
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            applySearchAndFilters()
        }
    }
    
    fun togglePlatform(platform: Platform) {
        val currentState = _uiState.value
        val newPlatforms = if (currentState.searchFilterState.selectedPlatforms.contains(platform)) {
            currentState.searchFilterState.selectedPlatforms - platform
        } else {
            currentState.searchFilterState.selectedPlatforms + platform
        }
        
        // Validate platform selection
        val validationResult = FilterValidator.validatePlatforms(
            newPlatforms,
            currentState.availablePlatforms
        )
        
        if (validationResult.hasError) {
            val error = (validationResult as ValidationResult.Error).error
            _uiState.update { 
                it.copy(
                    filterError = error,
                    canRetry = false
                )
            }
            return
        }
        
        _uiState.update { 
            it.copy(
                searchFilterState = it.searchFilterState.togglePlatform(platform),
                filterError = null
            )
        }
        applySearchAndFilters()
    }
    
    fun toggleGenre(genre: Genre) {
        _uiState.update { 
            it.copy(searchFilterState = it.searchFilterState.toggleGenre(genre))
        }
        applySearchAndFilters()
    }
    
    fun updateMinRating(rating: Double) {
        // Validate rating input
        val validationResult = FilterValidator.validateRating(rating)
        
        if (validationResult.hasError) {
            val error = (validationResult as ValidationResult.Error).error
            _uiState.update { 
                it.copy(
                    filterError = error,
                    canRetry = false
                )
            }
            return
        }
        
        _uiState.update { 
            it.copy(
                searchFilterState = it.searchFilterState.copy(minRating = rating),
                filterError = null
            )
        }
        applySearchAndFilters()
    }
    
    fun clearSearch() {
        _uiState.update { 
            it.copy(searchFilterState = it.searchFilterState.clearSearch())
        }
        applySearchAndFilters()
    }
    
    fun clearPlatformFilters() {
        _uiState.update { 
            it.copy(searchFilterState = it.searchFilterState.clearPlatforms())
        }
        applySearchAndFilters()
    }
    
    fun clearGenreFilters() {
        _uiState.update { 
            it.copy(searchFilterState = it.searchFilterState.clearGenres())
        }
        applySearchAndFilters()
    }
    
    fun clearRatingFilter() {
        _uiState.update { 
            it.copy(searchFilterState = it.searchFilterState.clearRating())
        }
        applySearchAndFilters()
    }
    
    fun clearAllFilters() {
        _uiState.update { 
            it.copy(searchFilterState = SearchFilterState())
        }
        applySearchAndFilters()
    }
    
    fun removePlatform(platform: Platform) {
        togglePlatform(platform)
    }
    
    fun removeGenre(genre: Genre) {
        toggleGenre(genre)
    }
    
    private fun applySearchAndFilters() {
        filterJob?.cancel()
        filterJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentState = _uiState.value
                val searchFilterState = currentState.searchFilterState
                
                // Performance optimization: only show loading for operations that might take time
                val shouldShowLoading = currentState.games.size > 100 || 
                                      searchFilterState.searchQuery.isNotEmpty() ||
                                      searchFilterState.hasActiveFilters()
                
                if (shouldShowLoading) {
                    _uiState.update { 
                        it.copy(
                            isFilterLoading = true,
                            filterError = null
                        ) 
                    }
                }
                
                // Create cache key for this search/filter combination
                val cacheKey = createCacheKey(searchFilterState)
                
                // Check cache first for performance
                val cachedResult = filterCache[cacheKey]
                if (cachedResult != null) {
                    _uiState.update { 
                        it.copy(
                            filteredGames = cachedResult,
                            isFilterLoading = false
                        )
                    }
                    return@launch
                }
                
                // First apply search with caching
                val searchedGames = if (searchFilterState.searchQuery.isNotEmpty()) {
                    val searchCacheKey = with(StringUtils) { searchFilterState.searchQuery.toLowerCaseCompat() }
                    searchCache[searchCacheKey] ?: run {
                        val result = searchGamesUseCase(
                            games = currentState.games,
                            query = searchFilterState.searchQuery
                        )
                        // Cache management: remove oldest entries if cache is full
                        if (searchCache.size >= maxCacheSize) {
                            val oldestKey = searchCache.keys.first()
                            searchCache.remove(oldestKey)
                        }
                        searchCache[searchCacheKey] = result
                        result
                    }
                } else {
                    currentState.games
                }
                
                // Then apply filters
                val filteredGames = if (searchFilterState.hasActiveFilters()) {
                    filterGamesUseCase(
                        games = searchedGames,
                        platforms = searchFilterState.selectedPlatforms,
                        genres = searchFilterState.selectedGenres,
                        minRating = searchFilterState.minRating
                    )
                } else {
                    searchedGames
                }
                
                // Cache the final result
                if (filterCache.size >= maxCacheSize) {
                    val oldestKey = filterCache.keys.first()
                    filterCache.remove(oldestKey)
                }
                filterCache[cacheKey] = filteredGames
                
                _uiState.update { 
                    it.copy(
                        filteredGames = filteredGames,
                        isFilterLoading = false,
                        filterError = null
                    )
                }
            } catch (exception: Exception) {
                val filterError = when {
                    exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("validation") } } == true ->
                        SearchFilterError.ValidationError("filter criteria")
                    exception.message?.let { with(SearchUtils) { it.containsIgnoreCase("network") } } == true ->
                        SearchFilterError.NetworkError
                    else -> SearchFilterError.UnknownError(exception.message ?: "Filter operation failed")
                }
                
                _uiState.update { 
                    it.copy(
                        isFilterLoading = false,
                        filterError = filterError,
                        canRetry = true
                    )
                }
            }
        }
    }
    
    private fun createCacheKey(searchFilterState: SearchFilterState): String {
        return buildString {
            append("q:${searchFilterState.searchQuery}")
            append("|p:${searchFilterState.selectedPlatforms.joinToString(",") { it.id.toString() }}")
            append("|g:${searchFilterState.selectedGenres.joinToString(",") { it.id.toString() }}")
            append("|r:${searchFilterState.minRating}")
        }
    }
    
    private fun clearCaches() {
        searchCache.clear()
        filterCache.clear()
    }
    
    // Collection-related methods
    
    fun loadCollections() {
        viewModelScope.launch {
            _uiState.update { it.copy(isCollectionsLoading = true) }
            
            getCollectionsUseCase().fold(
                onSuccess = { collectionsWithCount ->
                    val collections = collectionsWithCount.map { it.collection }
                    val gameCollectionMap = buildGameCollectionMap(collections)
                    
                    _uiState.update { 
                        it.copy(
                            collections = collections,
                            gameCollectionMap = gameCollectionMap,
                            isCollectionsLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update { 
                        it.copy(isCollectionsLoading = false)
                    }
                    // Silently fail for collections loading to not disrupt main game functionality
                }
            )
        }
    }
    
    fun showAddToCollectionDialog(game: Game) {
        _uiState.update { 
            it.copy(
                showAddToCollectionDialog = true,
                selectedGameForCollection = game
            )
        }
    }
    
    fun hideAddToCollectionDialog() {
        _uiState.update { 
            it.copy(
                showAddToCollectionDialog = false,
                selectedGameForCollection = null
            )
        }
    }
    
    fun addGameToCollection(collection: GameCollection) {
        val selectedGame = _uiState.value.selectedGameForCollection ?: return
        
        viewModelScope.launch {
            addGameToCollectionUseCase(
                collectionId = collection.id,
                gameId = selectedGame.id,
                confirmTransition = true
            ).fold(
                onSuccess = {
                    // Refresh collections to update the UI
                    loadCollections()
                    hideAddToCollectionDialog()
                },
                onFailure = { error ->
                    // Handle error - could show a snackbar or error dialog
                    hideAddToCollectionDialog()
                }
            )
        }
    }
    
    private fun buildGameCollectionMap(collections: List<GameCollection>): Map<Int, List<CollectionType>> {
        val gameCollectionMap = mutableMapOf<Int, MutableList<CollectionType>>()
        
        collections.forEach { collection ->
            collection.gameIds.forEach { gameId ->
                gameCollectionMap.getOrPut(gameId) { mutableListOf() }.add(collection.type)
            }
        }
        
        return gameCollectionMap.mapValues { it.value.toList() }
    }
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        filterJob?.cancel()
        clearCaches()
    }

}