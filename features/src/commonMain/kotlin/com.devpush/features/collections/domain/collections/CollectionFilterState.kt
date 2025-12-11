package com.devpush.features.collections.domain.collections

import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.Platform

/**
 * Filter and sort state for collection views with user rating support
 */
data class CollectionFilterState(
    val searchQuery: String = "",
    val selectedPlatforms: Set<Platform> = emptySet(),
    val selectedGenres: Set<Genre> = emptySet(),
    val minExternalRating: Double = 0.0,
    val minUserRating: Int = 0, // 0 means no filter, 1-5 for star ratings
    val maxUserRating: Int = 5,
    val showOnlyRated: Boolean = false, // Show only games with user ratings
    val showOnlyReviewed: Boolean = false, // Show only games with user reviews
    val sortBy: CollectionSortOption = CollectionSortOption.NAME_ASC
) {
    fun hasActiveFilters(): Boolean = 
        searchQuery.isNotEmpty() || 
        selectedPlatforms.isNotEmpty() || 
        selectedGenres.isNotEmpty() || 
        minExternalRating > 0.0 ||
        minUserRating > 0 ||
        maxUserRating < 5 ||
        showOnlyRated ||
        showOnlyReviewed
    
    fun hasActiveSearch(): Boolean = searchQuery.isNotEmpty()
    
    fun hasActivePlatformFilters(): Boolean = selectedPlatforms.isNotEmpty()
    
    fun hasActiveGenreFilters(): Boolean = selectedGenres.isNotEmpty()
    
    fun hasActiveExternalRatingFilter(): Boolean = minExternalRating > 0.0
    
    fun hasActiveUserRatingFilter(): Boolean = minUserRating > 0 || maxUserRating < 5
    
    fun hasActiveUserDataFilters(): Boolean = showOnlyRated || showOnlyReviewed
    
    fun clearSearch(): CollectionFilterState = copy(searchQuery = "")
    
    fun clearPlatforms(): CollectionFilterState = copy(selectedPlatforms = emptySet())
    
    fun clearGenres(): CollectionFilterState = copy(selectedGenres = emptySet())
    
    fun clearExternalRating(): CollectionFilterState = copy(minExternalRating = 0.0)
    
    fun clearUserRating(): CollectionFilterState = copy(minUserRating = 0, maxUserRating = 5)
    
    fun clearUserDataFilters(): CollectionFilterState = copy(showOnlyRated = false, showOnlyReviewed = false)
    
    fun clearAll(): CollectionFilterState = CollectionFilterState()
    
    fun togglePlatform(platform: Platform): CollectionFilterState {
        return if (selectedPlatforms.contains(platform)) {
            copy(selectedPlatforms = selectedPlatforms - platform)
        } else {
            copy(selectedPlatforms = selectedPlatforms + platform)
        }
    }
    
    fun toggleGenre(genre: Genre): CollectionFilterState {
        return if (selectedGenres.contains(genre)) {
            copy(selectedGenres = selectedGenres - genre)
        } else {
            copy(selectedGenres = selectedGenres + genre)
        }
    }
    
    fun setUserRatingRange(min: Int, max: Int): CollectionFilterState {
        return copy(
            minUserRating = min.coerceIn(0, 5),
            maxUserRating = max.coerceIn(min, 5)
        )
    }
    
    fun toggleShowOnlyRated(): CollectionFilterState = copy(showOnlyRated = !showOnlyRated)
    
    fun toggleShowOnlyReviewed(): CollectionFilterState = copy(showOnlyReviewed = !showOnlyReviewed)
}

/**
 * Sorting options for collection views
 */
enum class CollectionSortOption(
    val displayName: String,
    val description: String
) {
    NAME_ASC("Name A-Z", "Sort by game name (A to Z)"),
    NAME_DESC("Name Z-A", "Sort by game name (Z to A)"),
    EXTERNAL_RATING_DESC("External Rating (High to Low)", "Sort by external rating (highest first)"),
    EXTERNAL_RATING_ASC("External Rating (Low to High)", "Sort by external rating (lowest first)"),
    USER_RATING_DESC("Your Rating (High to Low)", "Sort by your rating (highest first)"),
    USER_RATING_ASC("Your Rating (Low to High)", "Sort by your rating (lowest first)"),
    RELEASE_DATE_DESC("Release Date (Newest)", "Sort by release date (newest first)"),
    RELEASE_DATE_ASC("Release Date (Oldest)", "Sort by release date (oldest first)"),
    RECENTLY_RATED("Recently Rated", "Sort by when you last rated them"),
    RECENTLY_REVIEWED("Recently Reviewed", "Sort by when you last reviewed them");
    
    companion object {
        fun getDefaultOptions(): List<CollectionSortOption> = listOf(
            NAME_ASC,
            NAME_DESC,
            EXTERNAL_RATING_DESC,
            EXTERNAL_RATING_ASC,
            RELEASE_DATE_DESC,
            RELEASE_DATE_ASC
        )
        
        fun getUserRatingOptions(): List<CollectionSortOption> = listOf(
            USER_RATING_DESC,
            USER_RATING_ASC,
            RECENTLY_RATED,
            RECENTLY_REVIEWED
        )
        
        fun getAllOptions(): List<CollectionSortOption> = values().toList()
    }
    
    fun requiresUserData(): Boolean = when (this) {
        USER_RATING_DESC, USER_RATING_ASC, RECENTLY_RATED, RECENTLY_REVIEWED -> true
        else -> false
    }
}