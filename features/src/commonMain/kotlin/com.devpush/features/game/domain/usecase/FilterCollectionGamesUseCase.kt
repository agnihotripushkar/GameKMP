package com.devpush.features.game.domain.usecase

import com.devpush.features.bookmarklist.domain.collections.CollectionFilterState
import com.devpush.features.bookmarklist.domain.collections.CollectionSortOption
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.common.utils.StringUtils

/**
 * Use case for filtering and sorting games in collections with user rating support
 */
class FilterCollectionGamesUseCase {
    
    /**
     * Filters and sorts a list of games with user data based on the provided filter state
     * @param games List of games with user data to filter and sort
     * @param filterState Current filter and sort state
     * @return Filtered and sorted list of games
     */
    operator fun invoke(
        games: List<GameWithUserData>,
        filterState: CollectionFilterState
    ): List<GameWithUserData> {
        return games
            .filter { gameWithUserData -> matchesFilters(gameWithUserData, filterState) }
            .sortedWith { a, b -> compareGames(a, b, filterState.sortBy) }
    }
    
    /**
     * Checks if a game matches the current filters
     */
    private fun matchesFilters(
        gameWithUserData: GameWithUserData,
        filterState: CollectionFilterState
    ): Boolean {
        val game = gameWithUserData.game
        
        // Search query filter
        if (filterState.hasActiveSearch()) {
            val query = with(StringUtils) { filterState.searchQuery.toLowerCaseCompat() }
            val gameName = with(StringUtils) { game.name.toLowerCaseCompat() }
            if (!gameName.contains(query)) {
                return false
            }
        }
        
        // Platform filter
        if (filterState.hasActivePlatformFilters()) {
            val hasMatchingPlatform = game.platforms.any { platform ->
                filterState.selectedPlatforms.contains(platform)
            }
            if (!hasMatchingPlatform) {
                return false
            }
        }
        
        // Genre filter
        if (filterState.hasActiveGenreFilters()) {
            val hasMatchingGenre = game.genres.any { genre ->
                filterState.selectedGenres.contains(genre)
            }
            if (!hasMatchingGenre) {
                return false
            }
        }
        
        // External rating filter
        if (filterState.hasActiveExternalRatingFilter()) {
            if (game.rating < filterState.minExternalRating) {
                return false
            }
        }
        
        // User rating filter
        if (filterState.hasActiveUserRatingFilter()) {
            val userRating = gameWithUserData.userRating?.rating ?: 0
            if (userRating < filterState.minUserRating || userRating > filterState.maxUserRating) {
                return false
            }
        }
        
        // Show only rated games filter
        if (filterState.showOnlyRated && !gameWithUserData.hasUserRating) {
            return false
        }
        
        // Show only reviewed games filter
        if (filterState.showOnlyReviewed && !gameWithUserData.hasUserReview) {
            return false
        }
        
        return true
    }
    
    /**
     * Compares two games for sorting based on the sort option
     */
    private fun compareGames(
        a: GameWithUserData,
        b: GameWithUserData,
        sortOption: CollectionSortOption
    ): Int {
        return when (sortOption) {
            CollectionSortOption.NAME_ASC -> with(StringUtils) { a.game.name.compareIgnoreCase(b.game.name) }
            CollectionSortOption.NAME_DESC -> with(StringUtils) { b.game.name.compareIgnoreCase(a.game.name) }
            
            CollectionSortOption.EXTERNAL_RATING_DESC -> {
                b.game.rating.compareTo(a.game.rating)
            }
            CollectionSortOption.EXTERNAL_RATING_ASC -> {
                a.game.rating.compareTo(b.game.rating)
            }
            
            CollectionSortOption.USER_RATING_DESC -> {
                val aRating = a.userRating?.rating ?: 0
                val bRating = b.userRating?.rating ?: 0
                bRating.compareTo(aRating)
            }
            CollectionSortOption.USER_RATING_ASC -> {
                val aRating = a.userRating?.rating ?: 0
                val bRating = b.userRating?.rating ?: 0
                aRating.compareTo(bRating)
            }
            
            CollectionSortOption.RELEASE_DATE_DESC -> {
                val aDate = a.game.releaseDate ?: ""
                val bDate = b.game.releaseDate ?: ""
                bDate.compareTo(aDate)
            }
            CollectionSortOption.RELEASE_DATE_ASC -> {
                val aDate = a.game.releaseDate ?: ""
                val bDate = b.game.releaseDate ?: ""
                aDate.compareTo(bDate)
            }
            
            CollectionSortOption.RECENTLY_RATED -> {
                val aTime = a.userRating?.updatedAt ?: 0L
                val bTime = b.userRating?.updatedAt ?: 0L
                bTime.compareTo(aTime)
            }
            
            CollectionSortOption.RECENTLY_REVIEWED -> {
                val aTime = a.userReview?.updatedAt ?: 0L
                val bTime = b.userReview?.updatedAt ?: 0L
                bTime.compareTo(aTime)
            }
        }
    }
}