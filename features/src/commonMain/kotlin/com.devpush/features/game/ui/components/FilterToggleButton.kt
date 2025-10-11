package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A toggle button for showing/hiding the filter panel with active filter count indicator.
 * 
 * @param isExpanded Whether the filter panel is currently expanded
 * @param activeFilterCount Number of active filters to show in badge
 * @param onClick Callback when the button is clicked
 * @param modifier Modifier for styling
 */
@Composable
fun FilterToggleButton(
    isExpanded: Boolean,
    activeFilterCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (activeFilterCount > 0) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = activeFilterCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = if (isExpanded) "Hide filters" else "Show filters",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Filters",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Preview
@Composable
fun FilterToggleButtonCollapsedPreview() {
    FilterToggleButton(
        isExpanded = false,
        activeFilterCount = 0,
        onClick = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun FilterToggleButtonExpandedPreview() {
    FilterToggleButton(
        isExpanded = true,
        activeFilterCount = 3,
        onClick = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun FilterToggleButtonWithFiltersPreview() {
    FilterToggleButton(
        isExpanded = false,
        activeFilterCount = 5,
        onClick = {},
        modifier = Modifier.padding(16.dp)
    )
}