package com.devpush.features.bookmarklist.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import com.devpush.features.ui.components.ExpressiveTextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.devpush.features.bookmarklist.domain.collections.CollectionFilterState
import com.devpush.features.bookmarklist.domain.collections.CollectionSortOption
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Filter panel component for collection views with user rating filters
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CollectionFilterPanel(
    filterState: CollectionFilterState,
    onFilterStateChanged: (CollectionFilterState) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortDropdown by remember { mutableStateOf(false) }
    var userRatingRange by remember { 
        mutableStateOf(filterState.minUserRating.toFloat()..filterState.maxUserRating.toFloat()) 
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with clear button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Filters & Sort",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (filterState.hasActiveFilters()) {
                    ExpressiveTextButton(
                        onClick = onClearFilters,
                        contentDescription = "Clear all collection filters"
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search
            ExpressiveOutlinedTextField(
                value = filterState.searchQuery,
                onValueChange = { query ->
                    onFilterStateChanged(filterState.copy(searchQuery = query))
                },
                label = { Text("Search games") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                contentDescription = "Search games in collection"
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort options
            Text(
                text = "Sort by",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box {
                FilterChip(
                    selected = true,
                    onClick = { showSortDropdown = true },
                    label = { Text(filterState.sortBy.displayName) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = showSortDropdown,
                    onDismissRequest = { showSortDropdown = false }
                ) {
                    CollectionSortOption.getAllOptions().forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.displayName) },
                            onClick = {
                                onFilterStateChanged(filterState.copy(sortBy = option))
                                showSortDropdown = false
                            },
                            leadingIcon = if (filterState.sortBy == option) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else null
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User rating filters
            Text(
                text = "Your Ratings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Show only rated/reviewed checkboxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onFilterStateChanged(filterState.toggleShowOnlyRated())
                    }
                ) {
                    Checkbox(
                        checked = filterState.showOnlyRated,
                        onCheckedChange = { onFilterStateChanged(filterState.toggleShowOnlyRated()) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Only rated")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable {
                        onFilterStateChanged(filterState.toggleShowOnlyReviewed())
                    }
                ) {
                    Checkbox(
                        checked = filterState.showOnlyReviewed,
                        onCheckedChange = { onFilterStateChanged(filterState.toggleShowOnlyReviewed()) }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Only reviewed")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // User rating range slider
            Text(
                text = "Rating Range: ${if (filterState.minUserRating > 0) filterState.minUserRating else "Any"} - ${filterState.maxUserRating} stars",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            RangeSlider(
                value = userRatingRange,
                onValueChange = { range ->
                    userRatingRange = range
                },
                onValueChangeFinished = {
                    onFilterStateChanged(
                        filterState.setUserRatingRange(
                            userRatingRange.start.toInt(),
                            userRatingRange.endInclusive.toInt()
                        )
                    )
                },
                valueRange = 0f..5f,
                steps = 4,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Star rating visual indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(6) { index ->
                    val rating = index
                    val isInRange = rating >= userRatingRange.start.toInt() && 
                                   rating <= userRatingRange.endInclusive.toInt()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isInRange && rating > 0) Icons.Filled.Star else Icons.Outlined.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = if (isInRange && rating > 0) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outline
                            }
                        )
                        Text(
                            text = if (rating == 0) "Any" else rating.toString(),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun CollectionFilterPanelPreview() {
    CollectionFilterPanel(
        filterState = CollectionFilterState(
            searchQuery = "Mario",
            minUserRating = 3,
            maxUserRating = 5,
            showOnlyRated = true,
            sortBy = CollectionSortOption.USER_RATING_DESC
        ),
        onFilterStateChanged = {},
        onClearFilters = {}
    )
}