package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.Platform
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A comprehensive filter panel for games with platform, genre, and rating filters.
 * 
 * @param availablePlatforms List of all available platforms
 * @param availableGenres List of all available genres
 * @param selectedPlatforms Set of currently selected platforms
 * @param selectedGenres Set of currently selected genres
 * @param minRating Current minimum rating filter value
 * @param onPlatformToggle Callback when a platform is selected/deselected
 * @param onGenreToggle Callback when a genre is selected/deselected
 * @param onRatingChange Callback when rating filter changes
 * @param onClearFilters Callback to clear all filters
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterPanel(
    availablePlatforms: List<Platform>,
    availableGenres: List<Genre>,
    selectedPlatforms: Set<Platform>,
    selectedGenres: Set<Genre>,
    minRating: Double,
    onPlatformToggle: (Platform) -> Unit,
    onGenreToggle: (Genre) -> Unit,
    onRatingChange: (Double) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with clear button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            ExpressiveOutlinedButton(
                onClick = onClearFilters,
                enabled = selectedPlatforms.isNotEmpty() || 
                         selectedGenres.isNotEmpty() || 
                         minRating > 0.0,
                contentDescription = "Clear all filters"
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Clear All")
            }
        }
        
        // Platform filters
        if (availablePlatforms.isNotEmpty()) {
            Column {
                Text(
                    text = "Platforms",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availablePlatforms.forEach { platform ->
                        FilterChip(
                            selected = selectedPlatforms.contains(platform),
                            onClick = { onPlatformToggle(platform) },
                            label = {
                                Text(
                                    text = platform.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Genre filters
        if (availableGenres.isNotEmpty()) {
            Column {
                Text(
                    text = "Genres",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    availableGenres.forEach { genre ->
                        FilterChip(
                            selected = selectedGenres.contains(genre),
                            onClick = { onGenreToggle(genre) },
                            label = {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Rating filter
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Minimum Rating",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = if (minRating > 0.0) "${(kotlin.math.round(minRating * 10) / 10.0)}+" else "Any",
                    style = MaterialTheme.typography.bodyMedium,

                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = minRating.toFloat(),
                onValueChange = { onRatingChange(it.toDouble()) },
                valueRange = 0f..5f,
                steps = 9, // 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0
                modifier = Modifier.fillMaxWidth()
            )
            
            // Rating scale labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "5.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview
@Composable
fun FilterPanelPreview() {
    val samplePlatforms = listOf(
        Platform(1, "PlayStation 5", "playstation5"),
        Platform(2, "Xbox Series X/S", "xbox-series-x"),
        Platform(3, "Nintendo Switch", "nintendo-switch"),
        Platform(4, "PC", "pc"),
        Platform(5, "PlayStation 4", "playstation4")
    )
    
    val sampleGenres = listOf(
        Genre(1, "Action", "action"),
        Genre(2, "Adventure", "adventure"),
        Genre(3, "RPG", "role-playing-games-rpg"),
        Genre(4, "Strategy", "strategy"),
        Genre(5, "Simulation", "simulation")
    )
    
    FilterPanel(
        availablePlatforms = samplePlatforms,
        availableGenres = sampleGenres,
        selectedPlatforms = setOf(samplePlatforms[0], samplePlatforms[3]),
        selectedGenres = setOf(sampleGenres[0]),
        minRating = 3.5,
        onPlatformToggle = {},
        onGenreToggle = {},
        onRatingChange = {},
        onClearFilters = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun FilterPanelEmptyPreview() {
    FilterPanel(
        availablePlatforms = emptyList(),
        availableGenres = emptyList(),
        selectedPlatforms = emptySet(),
        selectedGenres = emptySet(),
        minRating = 0.0,
        onPlatformToggle = {},
        onGenreToggle = {},
        onRatingChange = {},
        onClearFilters = {},
        modifier = Modifier.padding(16.dp)
    )
}