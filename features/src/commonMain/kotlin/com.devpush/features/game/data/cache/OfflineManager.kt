package com.devpush.features.game.data.cache

import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.SearchFilterError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * Manages offline functionality and cached data for the game search and filter feature.
 * Provides fallback data when network is unavailable and tracks data freshness.
 */
class OfflineManager {
    
    private val _isOnline = MutableStateFlow(true)
    val isOnline: Flow<Boolean> = _isOnline.asStateFlow()
    
    private val _cachedGames = MutableStateFlow<List<Game>>(emptyList())
    private val _cachedPlatforms = MutableStateFlow<List<Platform>>(emptyList())
    private val _cachedGenres = MutableStateFlow<List<Genre>>(emptyList())
    
    private var lastUpdateTime: Instant? = null
    private val maxCacheAge = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    
    // Cache metadata
    private data class CacheMetadata(
        val timestamp: Instant,
        val version: Int = 1,
        val source: String = "api"
    )
    
    private var gamesMetadata: CacheMetadata? = null
    private var platformsMetadata: CacheMetadata? = null
    private var genresMetadata: CacheMetadata? = null
    
    /**
     * Updates the online status
     */
    fun updateOnlineStatus(isOnline: Boolean) {
        _isOnline.value = isOnline
    }
    
    /**
     * Caches games data with metadata
     */
    fun cacheGames(games: List<Game>) {
        _cachedGames.value = games
        gamesMetadata = CacheMetadata(
            timestamp = Clock.System.now(),
            source = if (_isOnline.value) "api" else "offline"
        )
        lastUpdateTime = Clock.System.now()
    }
    
    /**
     * Caches platforms data with metadata
     */
    fun cachePlatforms(platforms: List<Platform>) {
        _cachedPlatforms.value = platforms
        platformsMetadata = CacheMetadata(
            timestamp = Clock.System.now(),
            source = if (_isOnline.value) "api" else "offline"
        )
    }
    
    /**
     * Caches genres data with metadata
     */
    fun cacheGenres(genres: List<Genre>) {
        _cachedGenres.value = genres
        genresMetadata = CacheMetadata(
            timestamp = Clock.System.now(),
            source = if (_isOnline.value) "api" else "offline"
        )
    }
    
    /**
     * Gets cached games with freshness validation
     */
    fun getCachedGames(): OfflineResult<List<Game>> {
        val games = _cachedGames.value
        val metadata = gamesMetadata
        
        return when {
            games.isEmpty() -> OfflineResult.NoData(
                SearchFilterError.CacheError
            )
            metadata == null -> OfflineResult.Success(
                data = games,
                warning = SearchFilterError.StaleDataError
            )
            !isDataFresh(metadata.timestamp) -> OfflineResult.Success(
                data = games,
                warning = SearchFilterError.StaleDataError
            )
            !_isOnline.value -> OfflineResult.Success(
                data = games,
                warning = SearchFilterError.OfflineError
            )
            else -> OfflineResult.Success(games)
        }
    }
    
    /**
     * Gets cached platforms with freshness validation
     */
    fun getCachedPlatforms(): OfflineResult<List<Platform>> {
        val platforms = _cachedPlatforms.value
        val metadata = platformsMetadata
        
        return when {
            platforms.isEmpty() -> OfflineResult.NoData(
                SearchFilterError.CacheError
            )
            metadata == null -> OfflineResult.Success(
                data = platforms,
                warning = SearchFilterError.StaleDataError
            )
            !isDataFresh(metadata.timestamp) -> OfflineResult.Success(
                data = platforms,
                warning = SearchFilterError.StaleDataError
            )
            !_isOnline.value -> OfflineResult.Success(
                data = platforms,
                warning = SearchFilterError.OfflineError
            )
            else -> OfflineResult.Success(platforms)
        }
    }
    
    /**
     * Gets cached genres with freshness validation
     */
    fun getCachedGenres(): OfflineResult<List<Genre>> {
        val genres = _cachedGenres.value
        val metadata = genresMetadata
        
        return when {
            genres.isEmpty() -> OfflineResult.NoData(
                SearchFilterError.CacheError
            )
            metadata == null -> OfflineResult.Success(
                data = genres,
                warning = SearchFilterError.StaleDataError
            )
            !isDataFresh(metadata.timestamp) -> OfflineResult.Success(
                data = genres,
                warning = SearchFilterError.StaleDataError
            )
            !_isOnline.value -> OfflineResult.Success(
                data = genres,
                warning = SearchFilterError.OfflineError
            )
            else -> OfflineResult.Success(genres)
        }
    }
    
    /**
     * Checks if cached data is still fresh
     */
    private fun isDataFresh(timestamp: Instant): Boolean {
        val now = Clock.System.now()
        val ageMillis = (now - timestamp).inWholeMilliseconds
        return ageMillis < maxCacheAge
    }
    
    /**
     * Gets cache status information
     */
    fun getCacheStatus(): CacheStatus {
        val now = Clock.System.now()
        val gamesAge = gamesMetadata?.let { (now - it.timestamp).inWholeMilliseconds }
        val platformsAge = platformsMetadata?.let { (now - it.timestamp).inWholeMilliseconds }
        val genresAge = genresMetadata?.let { (now - it.timestamp).inWholeMilliseconds }
        
        return CacheStatus(
            isOnline = _isOnline.value,
            hasGames = _cachedGames.value.isNotEmpty(),
            hasPlatforms = _cachedPlatforms.value.isNotEmpty(),
            hasGenres = _cachedGenres.value.isNotEmpty(),
            gamesAge = gamesAge,
            platformsAge = platformsAge,
            genresAge = genresAge,
            isDataFresh = gamesAge?.let { it < maxCacheAge } ?: false
        )
    }
    
    /**
     * Clears all cached data
     */
    fun clearCache() {
        _cachedGames.value = emptyList()
        _cachedPlatforms.value = emptyList()
        _cachedGenres.value = emptyList()
        gamesMetadata = null
        platformsMetadata = null
        genresMetadata = null
        lastUpdateTime = null
    }
    
    /**
     * Determines if we should use cached data based on current conditions
     */
    fun shouldUseCachedData(): Boolean {
        return !_isOnline.value || 
               (_cachedGames.value.isNotEmpty() && 
                gamesMetadata?.let { isDataFresh(it.timestamp) } == true)
    }
    
    /**
     * Gets appropriate error for current offline state
     */
    fun getOfflineError(): SearchFilterError? {
        return when {
            !_isOnline.value && _cachedGames.value.isEmpty() -> SearchFilterError.NetworkError
            !_isOnline.value -> SearchFilterError.OfflineError
            _cachedGames.value.isNotEmpty() && 
            gamesMetadata?.let { !isDataFresh(it.timestamp) } == true -> SearchFilterError.StaleDataError
            else -> null
        }
    }
}

/**
 * Result wrapper for offline operations
 */
sealed class OfflineResult<out T> {
    data class Success<T>(
        val data: T,
        val warning: SearchFilterError? = null
    ) : OfflineResult<T>()
    
    data class NoData(
        val error: SearchFilterError
    ) : OfflineResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val hasWarning: Boolean get() = this is Success && warning != null
}

/**
 * Cache status information
 */
data class CacheStatus(
    val isOnline: Boolean,
    val hasGames: Boolean,
    val hasPlatforms: Boolean,
    val hasGenres: Boolean,
    val gamesAge: Long?, // Age in milliseconds
    val platformsAge: Long?,
    val genresAge: Long?,
    val isDataFresh: Boolean
) {
    val hasAnyData: Boolean get() = hasGames || hasPlatforms || hasGenres
    val isFullyCached: Boolean get() = hasGames && hasPlatforms && hasGenres
}