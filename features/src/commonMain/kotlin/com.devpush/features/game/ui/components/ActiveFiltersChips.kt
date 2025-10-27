package com.devpush.features.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.game.domain.model.Platform
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Displays active search and filter criteria as removable chips.
 * 
 * @param searchQuery Current search query
 * @param selectedPlatforms Set of selected platforms
 * @param selectedGenres Set of selected genres
 * @param minRating Current minimum rating filter
 * @param onRemoveSearch Callback to remove search query
 * @param onRemovePlatform Callback to remove a specific platform filter
 * @param onRemoveGenre Callback to remove a specific genre filter
 * @param onRemoveRating Callback to remove rating filter
 * @param onClearAll Callback to clear all filters and search
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveFiltersChips(
    searchQuery: String,
    selectedPlatforms: Set<Platform>,
    selectedGenres: Set<Genre>,
    minRating: Double,
    onRemoveSearch: () -> Unit,
    onRemovePlatform: (Platform) -> Unit,
    onRemoveGenre: (Genre) -> Unit,
    onRemoveRating: () -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasActiveFilters = searchQuery.isNotEmpty() || 
                          selectedPlatforms.isNotEmpty() || 
                          selectedGenres.isNotEmpty() || 
                          minRating > 0.0
    
    // Animate the entire chips container
    AnimatedVisibility(
        visible = hasActiveFilters,
        enter = slideInHorizontally(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(animationSpec = tween(300)),
        exit = slideOutHorizontally(
            animationSpec = tween(200)
        ) + fadeOut(animationSpec = tween(200))
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlowRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Search query chip with animation
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                ) {
                    key("search_$searchQuery") {
                        AssistChip(
                            onClick = onRemoveSearch,
                            label = {
                                Text(
                                    text = "\"$searchQuery\"",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove search",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        )
                    }
                }
                
                // Platform chips with staggered animation
                selectedPlatforms.forEachIndexed { index, platform ->
                    key("platform_${platform.id}") {
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 50
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    delayMillis = index * 50
                                )
                            ),
                            exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                        ) {
                            AssistChip(
                                onClick = { onRemovePlatform(platform) },
                                label = {
                                    Text(
                                        text = platform.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${platform.name} filter",
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Genre chips with staggered animation
                selectedGenres.forEachIndexed { index, genre ->
                    key("genre_${genre.id}") {
                        AnimatedVisibility(
                            visible = true,
                            enter = scaleIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = (selectedPlatforms.size + index) * 50
                                )
                            ) + fadeIn(
                                animationSpec = tween(
                                    durationMillis = 200,
                                    delayMillis = (selectedPlatforms.size + index) * 50
                                )
                            ),
                            exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                        ) {
                            AssistChip(
                                onClick = { onRemoveGenre(genre) },
                                label = {
                                    Text(
                                        text = genre.name,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove ${genre.name} filter",
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            )
                        }
                    }
                }
                
                // Rating chip with animation
                AnimatedVisibility(
                    visible = minRating > 0.0,
                    enter = scaleIn(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    ) + fadeIn(animationSpec = tween(200)),
                    exit = scaleOut(animationSpec = tween(150)) + fadeOut(animationSpec = tween(150))
                ) {
                    key("rating_$minRating") {
                        AssistChip(
                            onClick = onRemoveRating,
                            label = {
                                Text(
                                    text = "Rating ${String.format("%.1f", minRating)}+",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove rating filter",
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        )
                    }
                }
            }
            
            // Clear all button with animation
            AnimatedVisibility(
                visible = hasActiveFilters,
                enter = slideInHorizontally(
                    initialOffsetX = { it / 2 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn(animationSpec = tween(300, delayMillis = 100)),
                exit = slideOutHorizontally(
                    targetOffsetX = { it / 2 },
                    animationSpec = tween(150)
                ) + fadeOut(animationSpec = tween(150))
            ) {
                ExpressiveOutlinedButton(
                    onClick = onClearAll,
                    modifier = Modifier.padding(start = 8.dp),
                    contentDescription = "Clear all active filters"
                ) {
                    Text(
                        text = "Clear All",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ActiveFiltersChipsPreview() {
    val samplePlatforms = setOf(
        Platform(1, "PlayStation 5", "playstation5"),
        Platform(4, "PC", "pc")
    )
    
    val sampleGenres = setOf(
        Genre(1, "Action", "action"),
        Genre(3, "RPG", "role-playing-games-rpg")
    )
    
    ActiveFiltersChips(
        searchQuery = "Super Mario",
        selectedPlatforms = samplePlatforms,
        selectedGenres = sampleGenres,
        minRating = 4.0,
        onRemoveSearch = {},
        onRemovePlatform = {},
        onRemoveGenre = {},
        onRemoveRating = {},
        onClearAll = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun ActiveFiltersChipsSearchOnlyPreview() {
    ActiveFiltersChips(
        searchQuery = "Zelda",
        selectedPlatforms = emptySet(),
        selectedGenres = emptySet(),
        minRating = 0.0,
        onRemoveSearch = {},
        onRemovePlatform = {},
        onRemoveGenre = {},
        onRemoveRating = {},
        onClearAll = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun ActiveFiltersChipsEmptyPreview() {
    ActiveFiltersChips(
        searchQuery = "",
        selectedPlatforms = emptySet(),
        selectedGenres = emptySet(),
        minRating = 0.0,
        onRemoveSearch = {},
        onRemovePlatform = {},
        onRemoveGenre = {},
        onRemoveRating = {},
        onClearAll = {},
        modifier = Modifier.padding(16.dp)
    )
}