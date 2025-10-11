package com.devpush.features.game.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.devpush.features.game.domain.repository.GameRepository
import com.devpush.features.game.domain.usecase.SearchGamesUseCase
import com.devpush.features.game.domain.usecase.FilterGamesUseCase
import com.devpush.features.game.domain.usecase.GetAvailableFiltersUseCase
import com.devpush.features.game.domain.usecase.FilterOptions
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.SearchFilterError
import com.devpush.features.game.domain.validation.FilterValidator
import com.devpush.features.game.domain.validation.ValidationResult
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
    private val getAvailableFiltersUseCase: GetAvailableFiltersUseCase
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
    }

    fun getGames() {
        // Clear caches when refreshing data
        clearCaches()
        
        flow {
            emit(gameRepository.getGames())
        }.flowOn(Dispatchers.IO)
            .catch { exception ->
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
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                filterError = SearchFilterError.FilterLoadError,
                                canRetry = true
                            )
                        }
                    }
                }.onFailure { exception ->
                    val errorType = when {
                        exception.message?.contains("network", ignoreCase = true) == true -> 
                            SearchFilterError.NetworkError
                        exception.message?.contains("database", ignoreCase = true) == true -> 
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
                    val searchCacheKey = searchFilterState.searchQuery.lowercase()
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
                    exception.message?.contains("validation", ignoreCase = true) == true ->
                        SearchFilterError.ValidationError("filter criteria")
                    exception.message?.contains("network", ignoreCase = true) == true ->
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
    
    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        filterJob?.cancel()
        clearCaches()
    }

}