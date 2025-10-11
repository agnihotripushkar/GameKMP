package com.devpush.features.game.domain.validation

import com.devpush.features.game.domain.model.SearchFilterState
import com.devpush.features.game.domain.model.SearchFilterError
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre

/**
 * Validator for search and filter combinations to ensure valid user input
 * and prevent invalid filter states that could cause errors
 */
object FilterValidator {
    
    // Configuration constants
    private const val MIN_SEARCH_LENGTH = 2
    private const val MAX_SEARCH_LENGTH = 100
    private const val MAX_PLATFORMS = 10
    private const val MAX_GENRES = 15
    private const val MIN_RATING = 0.0
    private const val MAX_RATING = 5.0
    
    /**
     * Validates the complete search filter state
     * @param searchFilterState The state to validate
     * @param availablePlatforms List of valid platforms
     * @param availableGenres List of valid genres
     * @return ValidationResult indicating success or specific error
     */
    fun validateSearchFilterState(
        searchFilterState: SearchFilterState,
        availablePlatforms: List<Platform> = emptyList(),
        availableGenres: List<Genre> = emptyList()
    ): ValidationResult {
        
        // Validate search query
        val searchValidation = validateSearchQuery(searchFilterState.searchQuery)
        if (!searchValidation.isValid) {
            return searchValidation
        }
        
        // Validate platforms
        val platformValidation = validatePlatforms(
            searchFilterState.selectedPlatforms,
            availablePlatforms
        )
        if (!platformValidation.isValid) {
            return platformValidation
        }
        
        // Validate genres
        val genreValidation = validateGenres(
            searchFilterState.selectedGenres,
            availableGenres
        )
        if (!genreValidation.isValid) {
            return genreValidation
        }
        
        // Validate rating
        val ratingValidation = validateRating(searchFilterState.minRating)
        if (!ratingValidation.isValid) {
            return ratingValidation
        }
        
        // Validate filter combinations
        val combinationValidation = validateFilterCombinations(searchFilterState)
        if (!combinationValidation.isValid) {
            return combinationValidation
        }
        
        return ValidationResult.Success
    }
    
    /**
     * Validates search query input
     */
    fun validateSearchQuery(query: String): ValidationResult {
        return when {
            query.isBlank() -> ValidationResult.Success // Empty query is valid (shows all)
            query.length < MIN_SEARCH_LENGTH -> ValidationResult.Error(
                SearchFilterError.SearchQueryTooShort
            )
            query.length > MAX_SEARCH_LENGTH -> ValidationResult.Error(
                SearchFilterError.ValidationError(
                    field = "search query",
                    reason = "must be less than $MAX_SEARCH_LENGTH characters"
                )
            )
            query.trim() != query -> ValidationResult.Error(
                SearchFilterError.ValidationError(
                    field = "search query",
                    reason = "cannot start or end with spaces"
                )
            )
            containsInvalidCharacters(query) -> ValidationResult.Error(
                SearchFilterError.ValidationError(
                    field = "search query",
                    reason = "contains invalid characters"
                )
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates platform selection
     */
    fun validatePlatforms(
        selectedPlatforms: Set<Platform>,
        availablePlatforms: List<Platform>
    ): ValidationResult {
        return when {
            selectedPlatforms.size > MAX_PLATFORMS -> ValidationResult.Error(
                SearchFilterError.TooManyFiltersError
            )
            availablePlatforms.isNotEmpty() && !availablePlatforms.containsAll(selectedPlatforms) -> {
                ValidationResult.Error(
                    SearchFilterError.ValidationError(
                        field = "platform",
                        reason = "contains invalid platform selection"
                    )
                )
            }
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates genre selection
     */
    fun validateGenres(
        selectedGenres: Set<Genre>,
        availableGenres: List<Genre>
    ): ValidationResult {
        return when {
            selectedGenres.size > MAX_GENRES -> ValidationResult.Error(
                SearchFilterError.TooManyFiltersError
            )
            availableGenres.isNotEmpty() && !availableGenres.containsAll(selectedGenres) -> {
                ValidationResult.Error(
                    SearchFilterError.ValidationError(
                        field = "genre",
                        reason = "contains invalid genre selection"
                    )
                )
            }
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates rating input
     */
    fun validateRating(rating: Double): ValidationResult {
        return when {
            rating < MIN_RATING -> ValidationResult.Error(
                SearchFilterError.ValidationError(
                    field = "rating",
                    reason = "cannot be less than $MIN_RATING"
                )
            )
            rating > MAX_RATING -> ValidationResult.Error(
                SearchFilterError.ValidationError(
                    field = "rating",
                    reason = "cannot be greater than $MAX_RATING"
                )
            )
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Validates filter combinations for logical consistency
     */
    fun validateFilterCombinations(searchFilterState: SearchFilterState): ValidationResult {
        val totalFilters = searchFilterState.selectedPlatforms.size + 
                          searchFilterState.selectedGenres.size +
                          if (searchFilterState.minRating > 0.0) 1 else 0 +
                          if (searchFilterState.searchQuery.isNotEmpty()) 1 else 0
        
        return when {
            totalFilters > 20 -> ValidationResult.Error(
                SearchFilterError.FilterCombinationError(
                    "Too many filters applied (maximum 20)"
                )
            )
            
            // Check for potentially conflicting combinations
            searchFilterState.selectedPlatforms.size > 5 && 
            searchFilterState.selectedGenres.size > 10 -> ValidationResult.Error(
                SearchFilterError.FilterCombinationError(
                    "Too many platforms and genres selected simultaneously"
                )
            )
            
            // Check for overly restrictive combinations
            searchFilterState.minRating >= 4.5 && 
            searchFilterState.selectedGenres.size > 5 -> ValidationResult.Warning(
                "This combination might return very few results"
            )
            
            else -> ValidationResult.Success
        }
    }
    
    /**
     * Checks for invalid characters in search query
     */
    private fun containsInvalidCharacters(query: String): Boolean {
        // Allow alphanumeric, spaces, and common punctuation
        val validPattern = Regex("^[a-zA-Z0-9\\s\\-_'\".,!?:()&]+$")
        return !validPattern.matches(query)
    }
    
    /**
     * Suggests corrections for common validation errors
     */
    fun suggestCorrection(error: SearchFilterError): String? {
        return when (error) {
            is SearchFilterError.SearchQueryTooShort -> 
                "Try adding more characters to your search"
            is SearchFilterError.TooManyFiltersError -> 
                "Remove some filters to narrow down your search"
            is SearchFilterError.ValidationError -> when (error.field) {
                "search query" -> "Use only letters, numbers, and basic punctuation"
                "rating" -> "Select a rating between 0 and 5 stars"
                "platform" -> "Choose from the available platforms only"
                "genre" -> "Choose from the available genres only"
                else -> null
            }
            is SearchFilterError.FilterCombinationError -> 
                "Try using fewer filters or different combinations"
            else -> null
        }
    }
}

/**
 * Result of validation operation
 */
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Warning(val message: String) : ValidationResult()
    data class Error(val error: SearchFilterError) : ValidationResult()
    
    val isValid: Boolean get() = this is Success || this is Warning
    val hasWarning: Boolean get() = this is Warning
    val hasError: Boolean get() = this is Error
}