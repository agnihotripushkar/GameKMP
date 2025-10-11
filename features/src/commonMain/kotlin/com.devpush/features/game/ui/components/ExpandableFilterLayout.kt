package com.devpush.features.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.SearchFilterState
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * An expandable layout that contains the filter toggle button and the filter panel.
 * Provides smooth animations and responsive design for different screen sizes.
 * 
 * @param searchFilterState Current search and filter state
 * @param availablePlatforms List of all available platforms
 * @param availableGenres List of all available genres
 * @param onPlatformToggle Callback when a platform is selected/deselected
 * @param onGenreToggle Callback when a genre is selected/deselected
 * @param onRatingChange Callback when rating filter changes
 * @param onClearFilters Callback to clear all filters
 * @param modifier Modifier for styling
 */
@Composable
fun ExpandableFilterLayout(
    searchFilterState: SearchFilterState,
    availablePlatforms: List<Platform>,
    availableGenres: List<Genre>,
    onPlatformToggle: (Platform) -> Unit,
    onGenreToggle: (Genre) -> Unit,
    onRatingChange: (Double) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    
    // Calculate active filter count
    val activeFilterCount = with(searchFilterState) {
        var count = 0
        if (selectedPlatforms.isNotEmpty()) count += selectedPlatforms.size
        if (selectedGenres.isNotEmpty()) count += selectedGenres.size
        if (minRating > 0.0) count += 1
        count
    }
    
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Filter toggle button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterToggleButton(
                isExpanded = isExpanded,
                activeFilterCount = activeFilterCount,
                onClick = { isExpanded = !isExpanded }
            )
        }
        
        // Expandable filter panel with enhanced animations
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                expandFrom = Alignment.Top,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + slideInVertically(
                initialOffsetY = { -it / 4 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 300,
                    delayMillis = 50
                )
            ),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Top,
                animationSpec = tween(durationMillis = 200)
            ) + slideOutVertically(
                targetOffsetY = { -it / 4 },
                animationSpec = tween(durationMillis = 200)
            ) + fadeOut(
                animationSpec = tween(durationMillis = 150)
            )
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                
                FilterPanel(
                    availablePlatforms = availablePlatforms,
                    availableGenres = availableGenres,
                    selectedPlatforms = searchFilterState.selectedPlatforms,
                    selectedGenres = searchFilterState.selectedGenres,
                    minRating = searchFilterState.minRating,
                    onPlatformToggle = onPlatformToggle,
                    onGenreToggle = onGenreToggle,
                    onRatingChange = onRatingChange,
                    onClearFilters = {
                        onClearFilters()
                        isExpanded = false
                    }
                )
            }
        }
    }
}

/**
 * A responsive filter layout that adapts to different screen sizes.
 * On larger screens, shows filters side by side. On smaller screens, uses expandable layout.
 * 
 * @param searchFilterState Current search and filter state
 * @param availablePlatforms List of all available platforms
 * @param availableGenres List of all available genres
 * @param onPlatformToggle Callback when a platform is selected/deselected
 * @param onGenreToggle Callback when a genre is selected/deselected
 * @param onRatingChange Callback when rating filter changes
 * @param onClearFilters Callback to clear all filters
 * @param isCompactLayout Whether to use compact (expandable) layout
 * @param modifier Modifier for styling
 */
@Composable
fun ResponsiveFilterLayout(
    searchFilterState: SearchFilterState,
    availablePlatforms: List<Platform>,
    availableGenres: List<Genre>,
    onPlatformToggle: (Platform) -> Unit,
    onGenreToggle: (Genre) -> Unit,
    onRatingChange: (Double) -> Unit,
    onClearFilters: () -> Unit,
    isCompactLayout: Boolean = true,
    modifier: Modifier = Modifier
) {
    if (isCompactLayout) {
        // Compact layout with expandable panel
        ExpandableFilterLayout(
            searchFilterState = searchFilterState,
            availablePlatforms = availablePlatforms,
            availableGenres = availableGenres,
            onPlatformToggle = onPlatformToggle,
            onGenreToggle = onGenreToggle,
            onRatingChange = onRatingChange,
            onClearFilters = onClearFilters,
            modifier = modifier
        )
    } else {
        // Wide layout with always visible filters
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterPanel(
                availablePlatforms = availablePlatforms,
                availableGenres = availableGenres,
                selectedPlatforms = searchFilterState.selectedPlatforms,
                selectedGenres = searchFilterState.selectedGenres,
                minRating = searchFilterState.minRating,
                onPlatformToggle = onPlatformToggle,
                onGenreToggle = onGenreToggle,
                onRatingChange = onRatingChange,
                onClearFilters = onClearFilters,
                modifier = Modifier.width(300.dp)
            )
        }
    }
}

@Preview
@Composable
fun ExpandableFilterLayoutPreview() {
    val samplePlatforms = listOf(
        Platform(1, "PlayStation 5", "playstation5"),
        Platform(2, "Xbox Series X/S", "xbox-series-x"),
        Platform(3, "Nintendo Switch", "nintendo-switch"),
        Platform(4, "PC", "pc")
    )
    
    val sampleGenres = listOf(
        Genre(1, "Action", "action"),
        Genre(2, "Adventure", "adventure"),
        Genre(3, "RPG", "role-playing-games-rpg")
    )
    
    val sampleState = SearchFilterState(
        selectedPlatforms = setOf(samplePlatforms[0]),
        selectedGenres = setOf(sampleGenres[0]),
        minRating = 3.0
    )
    
    ExpandableFilterLayout(
        searchFilterState = sampleState,
        availablePlatforms = samplePlatforms,
        availableGenres = sampleGenres,
        onPlatformToggle = {},
        onGenreToggle = {},
        onRatingChange = {},
        onClearFilters = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun ResponsiveFilterLayoutCompactPreview() {
    val samplePlatforms = listOf(
        Platform(1, "PlayStation 5", "playstation5"),
        Platform(4, "PC", "pc")
    )
    
    val sampleGenres = listOf(
        Genre(1, "Action", "action")
    )
    
    val sampleState = SearchFilterState()
    
    ResponsiveFilterLayout(
        searchFilterState = sampleState,
        availablePlatforms = samplePlatforms,
        availableGenres = sampleGenres,
        onPlatformToggle = {},
        onGenreToggle = {},
        onRatingChange = {},
        onClearFilters = {},
        isCompactLayout = true,
        modifier = Modifier.padding(16.dp)
    )
}