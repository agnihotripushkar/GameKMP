package com.devpush.features.game.domain.model

data class SearchFilterState(
    val searchQuery: String = "",
    val selectedPlatforms: Set<Platform> = emptySet(),
    val selectedGenres: Set<Genre> = emptySet(),
    val minRating: Double = 0.0
) {
    fun hasActiveFilters(): Boolean = 
        searchQuery.isNotEmpty() || 
        selectedPlatforms.isNotEmpty() || 
        selectedGenres.isNotEmpty() || 
        minRating > 0.0
    
    fun hasActiveSearch(): Boolean = searchQuery.isNotEmpty()
    
    fun hasActivePlatformFilters(): Boolean = selectedPlatforms.isNotEmpty()
    
    fun hasActiveGenreFilters(): Boolean = selectedGenres.isNotEmpty()
    
    fun hasActiveRatingFilter(): Boolean = minRating > 0.0
    
    fun clearSearch(): SearchFilterState = copy(searchQuery = "")
    
    fun clearPlatforms(): SearchFilterState = copy(selectedPlatforms = emptySet())
    
    fun clearGenres(): SearchFilterState = copy(selectedGenres = emptySet())
    
    fun clearRating(): SearchFilterState = copy(minRating = 0.0)
    
    fun clearAll(): SearchFilterState = SearchFilterState()
    
    fun togglePlatform(platform: Platform): SearchFilterState {
        return if (selectedPlatforms.contains(platform)) {
            copy(selectedPlatforms = selectedPlatforms - platform)
        } else {
            copy(selectedPlatforms = selectedPlatforms + platform)
        }
    }
    
    fun toggleGenre(genre: Genre): SearchFilterState {
        return if (selectedGenres.contains(genre)) {
            copy(selectedGenres = selectedGenres - genre)
        } else {
            copy(selectedGenres = selectedGenres + genre)
        }
    }
}