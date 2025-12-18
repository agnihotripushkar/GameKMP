package com.devpush.features.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.devpush.features.ui.components.ExpressiveTextButton
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
import com.devpush.features.game.domain.model.Game
import com.devpush.features.collections.domain.collections.CollectionType
import com.devpush.features.collections.domain.collections.GameCollection
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock


/**
 * Dialog for adding games to collections with existing status indicators and confirmation for status transitions.
 * 
 * @param game The game to add to collections
 * @param collections List of available collections
 * @param onDismiss Callback when dialog is dismissed
 * @param onAddToCollection Callback when a collection is selected for adding the game
 * @param modifier Modifier for styling
 */
@Composable
fun AddToCollectionDialog(
    game: Game,
    collections: List<GameCollection>,
    onDismiss: () -> Unit,
    onAddToCollection: (GameCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmationDialog by remember { mutableStateOf<CollectionTransition?>(null) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Add to Collection",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            Column {
                // Game info
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Collections list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(collections.sortedBy { it.type.sortOrder }) { collection ->
                        CollectionSelectionItem(
                            collection = collection,
                            gameAlreadyInCollection = collection.containsGame(game.id),
                            onClick = { selectedCollection ->
                                val currentCollection = collections.find { it.containsGame(game.id) && it.type.isStatusCollection() }
                                val targetCollection = selectedCollection
                                
                                // Check if this requires confirmation for status transitions
                                if (currentCollection != null && 
                                    targetCollection.type.isStatusCollection() && 
                                    currentCollection.type.requiresConfirmationToMoveTo(targetCollection.type)) {
                                    
                                    showConfirmationDialog = CollectionTransition(
                                        from = currentCollection,
                                        to = targetCollection,
                                        game = game
                                    )
                                } else {
                                    onAddToCollection(selectedCollection)
                                }
                            }
                        )
                    }
                }
                
                if (collections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No collections available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {}
    )
    
    // Confirmation dialog for status transitions
    showConfirmationDialog?.let { transition ->
        StatusTransitionConfirmationDialog(
            transition = transition,
            onConfirm = {
                onAddToCollection(transition.to)
                showConfirmationDialog = null
            },
            onDismiss = {
                showConfirmationDialog = null
            }
        )
    }
}

/**
 * Individual collection selection item with status indicators
 */
@Composable
private fun CollectionSelectionItem(
    collection: GameCollection,
    gameAlreadyInCollection: Boolean,
    onClick: (GameCollection) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(collection) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (gameAlreadyInCollection) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Collection type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = getCollectionTypeColor(collection.type).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionTypeIcon(collection.type),
                        contentDescription = collection.type.displayName,
                        tint = getCollectionTypeColor(collection.type),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${collection.getGameCount()} ${if (collection.getGameCount() == 1) "game" else "games"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Status indicator
            if (gameAlreadyInCollection) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Already in collection",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add to collection",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Confirmation dialog for status transitions between collections
 */
@Composable
private fun StatusTransitionConfirmationDialog(
    transition: CollectionTransition,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Collection Move",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = transition.game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = transition.from.type.getMoveMessage(transition.to.type) 
                        ?: "Move from ${transition.from.name} to ${transition.to.name}?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        },
        confirmButton = {
            ExpressiveTextButton(
                onClick = onConfirm,
                contentDescription = "Confirm add to collection"
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            ExpressiveTextButton(
                onClick = onDismiss,
                contentDescription = "Cancel add to collection"
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Data class representing a collection transition that requires confirmation
 */
private data class CollectionTransition(
    val from: GameCollection,
    val to: GameCollection,
    val game: Game
)

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
 * Gets the appropriate color for a collection type
 */
@Composable
private fun getCollectionTypeColor(type: CollectionType): Color {
    return when (type) {
        CollectionType.WISHLIST -> MaterialTheme.colorScheme.primary
        CollectionType.CURRENTLY_PLAYING -> MaterialTheme.colorScheme.secondary
        CollectionType.COMPLETED -> MaterialTheme.colorScheme.tertiary
        CollectionType.CUSTOM -> MaterialTheme.colorScheme.outline
    }
}

@Preview
@Composable
fun AddToCollectionDialogPreview() {
    val sampleGame = Game(
        id = 1,
        name = "Super Mario Bros.",
        imageUrl = "",
        platforms = emptyList(),
        genres = emptyList(),
        rating = 4.5,
        releaseDate = "1985-09-13"
    )
    
    val sampleCollections = listOf(
        GameCollection(
            id = "1",
            name = "Wishlist",
            type = CollectionType.WISHLIST,
            gameIds = listOf(2, 3),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()

        ),
        GameCollection(
            id = "2",
            name = "Currently Playing",
            type = CollectionType.CURRENTLY_PLAYING,
            gameIds = listOf(1), // Game is already in this collection
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()

        ),
        GameCollection(
            id = "3",
            name = "Completed",
            type = CollectionType.COMPLETED,
            gameIds = listOf(4, 5, 6),
            createdAt = Clock.System.now().toEpochMilliseconds(),
            updatedAt = Clock.System.now().toEpochMilliseconds()

        )
    )
    
    AddToCollectionDialog(
        game = sampleGame,
        collections = sampleCollections,
        onDismiss = {},
        onAddToCollection = {}
    )
}