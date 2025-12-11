package com.devpush.features.game.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.collections.domain.collections.GameCollection
import com.devpush.features.game.domain.usecase.CollectionWithCount
import com.devpush.features.ui.components.ExpressiveCard
import com.devpush.features.ui.components.ExpressiveCardDefaults
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Card component for displaying game collections in a grid layout.
 * Supports long-press menu for edit/delete actions and displays collection type indicators.
 * 
 * @param collection The game collection to display
 * @param onClick Callback when the card is clicked
 * @param onEdit Callback when edit action is selected from long-press menu
 * @param onDelete Callback when delete action is selected from long-press menu
 * @param modifier Modifier for styling
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CollectionCard(
    collection: CollectionWithCount,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewDetails: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showDropdownMenu by remember { mutableStateOf(false) }
    
    ExpressiveCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDropdownMenu = true }
            ),
        colors = ExpressiveCardDefaults.colors(
            containerColor = getCollectionCardColor(collection.type)
        ),
        contentDescription = "Collection card for ${collection.name} with ${collection.gameCount} games"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header with collection type icon and indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Collection type icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCollectionTypeIcon(collection.type),
                            contentDescription = collection.type.displayName,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Default collection indicator
                    if (collection.collection.isDefaultCollection()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Default",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Collection name
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Game count and collection type
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Game count
                    Text(
                        text = "${collection.gameCount} ${if (collection.gameCount == 1) "game" else "games"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                    
                    // Collection type label
                    if (collection.type != CollectionType.CUSTOM) {
                        Text(
                            text = collection.type.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
            
            // Long-press dropdown menu
            DropdownMenu(
                expanded = showDropdownMenu,
                onDismissRequest = { showDropdownMenu = false },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                // Edit option - always available
                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit")
                        }
                    },
                    onClick = {
                        showDropdownMenu = false
                        onEdit()
                    }
                )
                
                // Delete option - only for custom collections
                if (collection.type == CollectionType.CUSTOM) {
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            onDelete()
                        }
                    )
                } else {
                    // Show disabled delete option for default collections with explanation
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Cannot delete",
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Delete",
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                    Text(
                                        text = "Default collections cannot be deleted",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            // Still call onDelete to show the protection dialog
                            onDelete()
                        },
                        enabled = false
                    )
                }
                
                // View Details option - if callback provided
                onViewDetails?.let { viewDetailsCallback ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "View details",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("View Details")
                            }
                        },
                        onClick = {
                            showDropdownMenu = false
                            viewDetailsCallback()
                        }
                    )
                }
                
                // Share option - if callback provided and collection has games
                if (collection.gameCount > 0) {
                    onShare?.let { shareCallback ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share collection",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Share Collection")
                                }
                            },
                            onClick = {
                                showDropdownMenu = false
                                shareCallback()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Gets the appropriate icon for a collection type
 */
@Composable
private fun getCollectionTypeIcon(type: CollectionType): ImageVector {
    return when (type) {
        CollectionType.WISHLIST -> Icons.Default.Favorite
        CollectionType.CURRENTLY_PLAYING -> Icons.Default.PlayArrow
        CollectionType.COMPLETED -> Icons.Default.Star
        CollectionType.CUSTOM -> Icons.Default.VideoLibrary
    }
}

/**
 * Gets the appropriate background color for a collection type
 */
@Composable
private fun getCollectionCardColor(type: CollectionType): Color {
    return when (type) {
        CollectionType.WISHLIST -> MaterialTheme.colorScheme.primaryContainer
        CollectionType.CURRENTLY_PLAYING -> MaterialTheme.colorScheme.secondaryContainer
        CollectionType.COMPLETED -> MaterialTheme.colorScheme.tertiaryContainer
        CollectionType.CUSTOM -> MaterialTheme.colorScheme.surfaceVariant
    }
}

@Preview
@Composable
fun CollectionCardPreview() {
    CollectionCard(
        collection = CollectionWithCount(
            collection = GameCollection(
                id = "1",
                name = "My Wishlist",
                type = CollectionType.WISHLIST,
                gameIds = listOf(1, 2, 3, 4, 5),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 5
        ),
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}

@Preview
@Composable
fun CollectionCardCustomPreview() {
    CollectionCard(
        collection = CollectionWithCount(
            collection = GameCollection(
                id = "2",
                name = "Indie Games Collection",
                type = CollectionType.CUSTOM,
                gameIds = listOf(1, 2),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 2
        ),
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}

@Preview
@Composable
fun CollectionCardEmptyPreview() {
    CollectionCard(
        collection = CollectionWithCount(
            collection = GameCollection(
                id = "3",
                name = "Currently Playing",
                type = CollectionType.CURRENTLY_PLAYING,
                gameIds = emptyList(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 0
        ),
        onClick = {},
        onEdit = {},
        onDelete = {}
    )
}