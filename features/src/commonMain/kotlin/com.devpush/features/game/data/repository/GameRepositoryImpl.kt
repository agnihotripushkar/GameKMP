package com.devpush.features.game.data.repository

import com.devpush.coreDatabase.AppDatabase
import com.devpush.coreNetwork.apiService.ApiService
import com.devpush.features.game.data.mappers.toDomainListOfGames
import com.devpush.features.game.data.mappers.toDomainGenres
import com.devpush.features.game.data.mappers.toDomainPlatforms
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.SearchFilterError
import com.devpush.features.game.domain.repository.GameRepository
import com.devpush.features.game.domain.validation.FilterValidator
import com.devpush.features.game.domain.validation.ValidationResult
import com.devpush.features.game.data.cache.OfflineManager
import com.devpush.features.game.data.cache.OfflineResult
import com.devpush.features.common.utils.SearchUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class GameRepositoryImpl(
    private val apiService: ApiService,
    private val appDatabase: AppDatabase,
    private val offlineManager: OfflineManager = OfflineManager()
): GameRepository {
    
    // Performance optimization: Enhanced caching with thread safety
    private var cachedPlatforms: List<Platform>? = null
    private var cachedGenres: List<Genre>? = null
    private var cachedGames: List<Game>? = null
    
    // Search result cache with LRU-like behavior (keep last 10 searches)
    private val searchCache = mutableMapOf<String, List<Game>>()
    private val searchCacheKeys = mutableListOf<String>()
    private val maxCacheSize = 10
    
    // Filter result cache
    private val filterCache = mutableMapOf<String, List<Game>>()
    private val filterCacheKeys = mutableListOf<String>()
    
    // Mutex for thread-safe cache operations
    private val cacheMutex = Mutex()
    
    // Cache timestamps for invalidation (5 minutes)
    private var cacheTimestamp = 0L
    private val cacheValidityDuration = 5 * 60 * 1000L // 5 minutes
    
    override suspend fun getGames(): Result<List<Game>> {
        return cacheMutex.withLock {
            try {
                // Check if cache is still valid
                val currentTime = System.currentTimeMillis()
                if (cachedGames != null && (currentTime - cacheTimestamp) < cacheValidityDuration) {
                    return@withLock Result.success(cachedGames!!)
                }
                
                // Try to get fresh data from API
                val result = apiService.getGames()
                return@withLock if (result.isSuccess) {
                    val games = result.getOrThrow().results.toDomainListOfGames()
                    cachedGames = games
                    cacheTimestamp = currentTime
                    
                    // Update offline manager cache
                    offlineManager.cacheGames(games)
                    offlineManager.updateOnlineStatus(true)
                    
                    // Clear search and filter caches when base data changes
                    clearCaches()
                    Result.success(games)
                } else {
                    // Network failed, try offline cache
                    handleNetworkFailure(result.exceptionOrNull())
                }
            } catch (exception: Exception) {
                handleNetworkFailure(exception)
            }
        }
    }
    
    private suspend fun handleNetworkFailure(exception: Throwable?): Result<List<Game>> {
        offlineManager.updateOnlineStatus(false)
        
        return when (val cachedResult = offlineManager.getCachedGames()) {
            is OfflineResult.Success -> {
                // Update local cache with offline data
                cachedGames = cachedResult.data
                cacheTimestamp = System.currentTimeMillis()
                
                // Return success but with warning if data is stale
                Result.success(cachedResult.data)
            }
            is OfflineResult.NoData -> {
                val error = determineNetworkError(exception)
                Result.failure(error)
            }
        }
    }
    
    private fun determineNetworkError(exception: Throwable?): SearchFilterError {
        return when {
            exception?.message?.let { with(SearchUtils) { it.containsIgnoreCase("timeout") } } == true -> 
                SearchFilterError.TimeoutError
            exception?.message?.let { with(SearchUtils) { it.containsIgnoreCase("server") } } == true -> 
                SearchFilterError.ServerError
            exception?.message?.let { with(SearchUtils) { it.containsIgnoreCase("network") } } == true -> 
                SearchFilterError.NetworkError
            else -> SearchFilterError.UnknownError(
                exception?.message ?: "Network request failed",
                exception
            )
        }
    }
    
    override suspend fun getGames(searchFilterState: SearchFilterState): Result<List<Game>> {
        return try {
            // Validate search filter state first
            val validationResult = FilterValidator.validateSearchFilterState(
                searchFilterState,
                cachedPlatforms ?: emptyList(),
                cachedGenres ?: emptyList()
            )
            
            if (validationResult.hasError) {
                val error = (validationResult as ValidationResult.Error).error
                return Result.failure(error)
            }
            
            // Performance optimization: Check filter cache first
            val filterKey = generateFilterCacheKey(searchFilterState)
            cacheMutex.withLock {
                filterCache[filterKey]?.let { cachedResult ->
                    return Result.success(cachedResult)
                }
            }
            
            // First get all games
            val allGamesResult = getGames()
            if (allGamesResult.isFailure) {
                return allGamesResult
            }
            
            val allGames = allGamesResult.getOrThrow()
            
            // Check for performance constraints
            if (allGames.size > 10000 && searchFilterState.hasComplexFilters()) {
                return Result.failure(SearchFilterError.PerformanceError)
            }
            
            val filteredGames = applyFiltersOptimized(allGames, searchFilterState)
            
            // Check if no results found
            if (filteredGames.isEmpty() && searchFilterState.hasActiveFilters()) {
                return Result.failure(SearchFilterError.NoResultsFound)
            }
            
            // Cache the result
            cacheMutex.withLock {
                cacheFilterResult(filterKey, filteredGames)
            }
            
            Result.success(filteredGames)
        } catch (e: Exception) {
            val error = when {
                e.message?.let { with(SearchUtils) { it.containsIgnoreCase("memory") } } == true -> 
                    SearchFilterError.MemoryError
                e.message?.let { with(SearchUtils) { it.containsIgnoreCase("timeout") } } == true -> 
                    SearchFilterError.PerformanceError
                else -> SearchFilterError.SearchProcessingError
            }
            Result.failure(error)
        }
    }

    override suspend fun searchGames(query: String): Result<List<Game>> {
        return if (query.isBlank()) {
            getGames()
        } else {
            // Performance optimization: Check search cache first
            val normalizedQuery = with(StringUtils) { query.trim().toLowerCaseCompat() }
            cacheMutex.withLock {
                searchCache[normalizedQuery]?.let { cachedResult ->
                    return Result.success(cachedResult)
                }
            }
            
            val result = apiService.search(query)
            return if (result.isSuccess) {
                val games = result.getOrThrow().results.toDomainListOfGames()
                
                // Cache the search result
                cacheMutex.withLock {
                    cacheSearchResult(normalizedQuery, games)
                }
                
                Result.success(games)
            } else {
                Result.failure(result.exceptionOrNull() ?: Exception("Search failed"))
            }
        }
    }
    
    override suspend fun getAvailablePlatforms(): Result<List<Platform>> {
        return try {
            cacheMutex.withLock {
                // Return cached platforms if available and valid
                val currentTime = System.currentTimeMillis()
                if (cachedPlatforms != null && (currentTime - cacheTimestamp) < cacheValidityDuration) {
                    return@withLock Result.success(cachedPlatforms!!)
                }
            }
            
            // Otherwise, extract platforms from games
            val gamesResult = getGames()
            if (gamesResult.isFailure) {
                return Result.failure(gamesResult.exceptionOrNull() ?: Exception("Failed to get games"))
            }
            
            val games = gamesResult.getOrThrow()
            // Performance optimization: Use sequence for large datasets
            val platforms = games.asSequence()
                .flatMap { it.platforms }
                .distinctBy { it.id }
                .sortedBy { it.name }
                .toList()
            
            cacheMutex.withLock {
                cachedPlatforms = platforms
            }
            Result.success(platforms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAvailableGenres(): Result<List<Genre>> {
        return try {
            cacheMutex.withLock {
                // Return cached genres if available and valid
                val currentTime = System.currentTimeMillis()
                if (cachedGenres != null && (currentTime - cacheTimestamp) < cacheValidityDuration) {
                    return@withLock Result.success(cachedGenres!!)
                }
            }
            
            // Otherwise, extract genres from games
            val gamesResult = getGames()
            if (gamesResult.isFailure) {
                return Result.failure(gamesResult.exceptionOrNull() ?: Exception("Failed to get games"))
            }
            
            val games = gamesResult.getOrThrow()
            // Performance optimization: Use sequence for large datasets
            val genres = games.asSequence()
                .flatMap { it.genres }
                .distinctBy { it.id }
                .sortedBy { it.name }
                .toList()
            
            cacheMutex.withLock {
                cachedGenres = genres
            }
            Result.success(genres)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun save(id: Int, image: String, name: String) {
        appDatabase.appDatabaseQueries.upsert(id.toLong(), name, image, 0.0, null)
    }

    override suspend fun delete(id: Int) {
        appDatabase.appDatabaseQueries.delete(id.toLong())
    }
    
    // Performance optimized filtering using sequences for large datasets
    private fun applyFiltersOptimized(games: List<Game>, searchFilterState: SearchFilterState): List<Game> {
        return games.asSequence()
            .let { sequence ->
                // Apply search filter first (most selective)
                if (searchFilterState.hasActiveSearch()) {
                    sequence.filter { game ->
                        with(SearchUtils) { game.name.containsIgnoreCase(searchFilterState.searchQuery) }
                    }
                } else sequence
            }
            .let { sequence ->
                // Apply rating filter (numeric comparison is fast)
                if (searchFilterState.hasActiveRatingFilter()) {
                    sequence.filter { game ->
                        game.rating >= searchFilterState.minRating
                    }
                } else sequence
            }
            .let { sequence ->
                // Apply platform filter
                if (searchFilterState.hasActivePlatformFilters()) {
                    val selectedPlatformIds = searchFilterState.selectedPlatforms.map { it.id }.toSet()
                    sequence.filter { game ->
                        game.platforms.any { platform ->
                            selectedPlatformIds.contains(platform.id)
                        }
                    }
                } else sequence
            }
            .let { sequence ->
                // Apply genre filter
                if (searchFilterState.hasActiveGenreFilters()) {
                    val selectedGenreIds = searchFilterState.selectedGenres.map { it.id }.toSet()
                    sequence.filter { game ->
                        game.genres.any { genre ->
                            selectedGenreIds.contains(genre.id)
                        }
                    }
                } else sequence
            }
            .toList()
    }
    
    // Cache management methods
    private fun generateFilterCacheKey(searchFilterState: SearchFilterState): String {
        return buildString {
            append("search:${searchFilterState.searchQuery}")
            append("|platforms:${searchFilterState.selectedPlatforms.map { it.id }.sorted().joinToString(",")}")
            append("|genres:${searchFilterState.selectedGenres.map { it.id }.sorted().joinToString(",")}")
            append("|rating:${searchFilterState.minRating}")
        }
    }
    
    private fun cacheSearchResult(query: String, games: List<Game>) {
        if (searchCacheKeys.size >= maxCacheSize) {
            // Remove oldest entry (LRU-like behavior)
            val oldestKey = searchCacheKeys.removeFirst()
            searchCache.remove(oldestKey)
        }
        searchCache[query] = games
        searchCacheKeys.add(query)
    }
    
    private fun cacheFilterResult(filterKey: String, games: List<Game>) {
        if (filterCacheKeys.size >= maxCacheSize) {
            // Remove oldest entry (LRU-like behavior)
            val oldestKey = filterCacheKeys.removeFirst()
            filterCache.remove(oldestKey)
        }
        filterCache[filterKey] = games
        filterCacheKeys.add(filterKey)
    }
    
    private fun clearCaches() {
        searchCache.clear()
        searchCacheKeys.clear()
        filterCache.clear()
        filterCacheKeys.clear()
    }
    
    // Pagination support for large result sets
    suspend fun getGamesPaginated(
        searchFilterState: SearchFilterState,
        page: Int = 0,
        pageSize: Int = 20
    ): Result<Pair<List<Game>, Boolean>> {
        return try {
            // Validate pagination parameters
            if (page < 0 || pageSize <= 0 || pageSize > 100) {
                return Result.failure(
                    SearchFilterError.ValidationError(
                        field = "pagination",
                        reason = "invalid page or pageSize parameters"
                    )
                )
            }
            
            val allGamesResult = getGames(searchFilterState)
            if (allGamesResult.isFailure) {
                return Result.failure(allGamesResult.exceptionOrNull() ?: Exception("Failed to get games"))
            }
            
            val allGames = allGamesResult.getOrThrow()
            val startIndex = page * pageSize
            val endIndex = minOf(startIndex + pageSize, allGames.size)
            
            if (startIndex >= allGames.size) {
                return Result.success(Pair(emptyList(), false))
            }
            
            val paginatedGames = allGames.subList(startIndex, endIndex)
            val hasMore = endIndex < allGames.size
            
            Result.success(Pair(paginatedGames, hasMore))
        } catch (e: Exception) {
            val error = when {
                e is OutOfMemoryError -> SearchFilterError.MemoryError
                else -> SearchFilterError.UnknownError(e.message ?: "Pagination failed", e)
            }
            Result.failure(error)
        }
    }
    
    /**
     * Gets offline cache status for UI display
     */
    fun getCacheStatus() = offlineManager.getCacheStatus()
    
    /**
     * Clears all caches including offline cache
     */
    fun clearAllCaches() {
        clearCaches()
        offlineManager.clearCache()
    }
    
    /**
     * Checks if the search filter state has complex filters that might impact performance
     */
    private fun SearchFilterState.hasComplexFilters(): Boolean {
        return selectedPlatforms.size > 5 || 
               selectedGenres.size > 10 || 
               (searchQuery.isNotEmpty() && searchQuery.length > 20) ||
               (selectedPlatforms.isNotEmpty() && selectedGenres.isNotEmpty() && minRating > 0.0)
    }

}