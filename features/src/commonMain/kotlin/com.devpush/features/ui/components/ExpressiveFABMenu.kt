package com.devpush.features.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp



/**
 * Simplified FAB menu for common use cases with predefined actions and accessibility support.
 */
@Composable
fun ExpressiveFABMenu(
    isExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAddToCollection: () -> Unit,
    onRateGame: () -> Unit,
    onWriteReview: () -> Unit,
    onShareGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .semantics {
                contentDescription = if (isExpanded) {
                    "Game actions menu expanded with 4 actions available"
                } else {
                    "Game actions menu collapsed. Tap to open 4 actions"
                }
                liveRegion = LiveRegionMode.Polite
            }
    ) {
        // FAB menu content positioned at bottom-right
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Expandable FAB actions
            if (isExpanded) {
                SmallFloatingActionButton(
                    onClick = {
                        onShareGame()
                        onExpandedChange(false)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Share this game with others"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null
                    )
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onWriteReview()
                        onExpandedChange(false)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Write a review for this game"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null
                    )
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onRateGame()
                        onExpandedChange(false)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Rate this game"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null
                    )
                }
                
                SmallFloatingActionButton(
                    onClick = {
                        onAddToCollection()
                        onExpandedChange(false)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = "Add this game to your collection"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
            }
            
            // Main FAB
            FloatingActionButton(
                onClick = { onExpandedChange(!isExpanded) },
                modifier = Modifier.semantics {
                    contentDescription = if (isExpanded) {
                        "Close game actions menu"
                    } else {
                        "Open game actions menu"
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    }
}