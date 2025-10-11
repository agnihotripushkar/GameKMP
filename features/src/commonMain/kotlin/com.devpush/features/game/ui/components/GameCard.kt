package com.devpush.features.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.collections.CollectionType
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.Locale

/**
 * Enhanced GameCard component with search result highlighting support and collection actions.
 * 
 * @param gameItem The game to display
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for styling
 * @param searchQuery Optional search query for highlighting matches in the game name
 * @param onAddToCollection Callback when "Add to Collection" is clicked
 * @param collectionsContainingGame List of collection types that contain this game (for visual indicators)
 */
@Composable
fun GameCard(
    gameItem: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onAddToCollection: ((Game) -> Unit)? = null,
    collectionsContainingGame: List<CollectionType> = emptyList()
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(Modifier.fillMaxSize()) {
            AsyncImage(
                model = gameItem.imageUrl,
                contentDescription = gameItem.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Collection status indicators (top-left corner)
            if (collectionsContainingGame.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    collectionsContainingGame.take(3).forEach { collectionType ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    color = getCollectionTypeColor(collectionType).copy(alpha = 0.9f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getCollectionTypeIcon(collectionType),
                                contentDescription = collectionType.displayName,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
                    // Show "+X" indicator if there are more than 3 collections
                    if (collectionsContainingGame.size > 3) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${collectionsContainingGame.size - 3}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                            )
                        }
                    }
                }
            }
            
            // Add to Collection button (top-right corner)
            if (onAddToCollection != null) {
                SmallFloatingActionButton(
                    onClick = { onAddToCollection(gameItem) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = if (collectionsContainingGame.isNotEmpty()) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = if (collectionsContainingGame.isNotEmpty()) "In collections" else "Add to collection",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Game title (bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f),
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = buildHighlightedText(gameItem.name, searchQuery),
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Builds an AnnotatedString with highlighted search matches.
 * 
 * @param text The original text
 * @param searchQuery The search query to highlight
 * @return AnnotatedString with highlighted matches
 */
@Composable
private fun buildHighlightedText(text: String, searchQuery: String) = buildAnnotatedString {
    if (searchQuery.isEmpty() || searchQuery.length < 2) {
        append(text)
        return@buildAnnotatedString
    }
    
    val lowerText = text.lowercase(Locale.getDefault())
    val lowerQuery = searchQuery.lowercase(Locale.getDefault())
    
    var startIndex = 0
    var matchIndex = lowerText.indexOf(lowerQuery, startIndex)
    
    while (matchIndex != -1) {
        // Add text before the match
        if (matchIndex > startIndex) {
            append(text.substring(startIndex, matchIndex))
        }
        
        // Add highlighted match
        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Bold,
                background = Color.Yellow.copy(alpha = 0.3f)
            )
        ) {
            append(text.substring(matchIndex, matchIndex + searchQuery.length))
        }
        
        startIndex = matchIndex + searchQuery.length
        matchIndex = lowerText.indexOf(lowerQuery, startIndex)
    }
    
    // Add remaining text
    if (startIndex < text.length) {
        append(text.substring(startIndex))
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
        CollectionType.CUSTOM -> Icons.Default.Add
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
fun GameCardPreview() {
    GameCard(
        gameItem = Game(
            id = 1,
            name = "Super Mario Bros.",
            imageUrl = "",
            platforms = emptyList(),
            genres = emptyList(),
            rating = 4.5,
            releaseDate = "1985-09-13"
        ),
        onClick = {}
    )
}

@Preview
@Composable
fun GameCardWithHighlightPreview() {
    GameCard(
        gameItem = Game(
            id = 1,
            name = "Super Mario Bros.",
            imageUrl = "",
            platforms = emptyList(),
            genres = emptyList(),
            rating = 4.5,
            releaseDate = "1985-09-13"
        ),
        onClick = {},
        searchQuery = "Mario"
    )
}

@Preview
@Composable
fun GameCardWithCollectionsPreview() {
    GameCard(
        gameItem = Game(
            id = 1,
            name = "Super Mario Bros.",
            imageUrl = "",
            platforms = emptyList(),
            genres = emptyList(),
            rating = 4.5,
            releaseDate = "1985-09-13"
        ),
        onClick = {},
        onAddToCollection = {},
        collectionsContainingGame = listOf(
            CollectionType.WISHLIST,
            CollectionType.CURRENTLY_PLAYING
        )
    )
}