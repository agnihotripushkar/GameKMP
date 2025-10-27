package com.devpush.features.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview
import com.devpush.features.userRatingsReviews.ui.components.QuickRating
import com.devpush.features.ui.components.ExpressiveCard
import com.devpush.features.common.utils.DateTimeUtils
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Game card component for collection detail view with remove action and game information display.
 * Enhanced with user rating and review indicators.
 * 
 * @param gameWithUserData The game with user data to display
 * @param onClick Callback when the card is clicked to view game details
 * @param onRemove Callback when the remove action is confirmed
 * @param modifier Modifier for styling
 * @param showRemoveButton Whether to show the remove button (default: true)
 * @param onQuickRating Callback for quick rating action (optional)
 * @param onReviewPreview Callback when review indicator is tapped (optional)
 */
@Composable
fun CollectionGameCard(
    gameWithUserData: GameWithUserData,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = true,
    onQuickRating: ((Int) -> Unit)? = null,
    onReviewPreview: (() -> Unit)? = null
) {
    val game = gameWithUserData.game
    var showRemoveConfirmation by remember { mutableStateOf(false) }
    var showQuickRatingDialog by remember { mutableStateOf(false) }
    
    ExpressiveCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        contentDescription = "Collection game card for ${game.name}"
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Game image background
            AsyncImage(
                model = game.imageUrl,
                contentDescription = game.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            
            // Gradient overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
            )
            
            // User rating badge (top-left corner)
            if (gameWithUserData.hasUserRating) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "User rating",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = gameWithUserData.userRating!!.rating.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Review indicator (top-center)
            if (gameWithUserData.hasUserReview) {
                IconButton(
                    onClick = { onReviewPreview?.invoke() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = "View review",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Remove button (top-right corner)
            if (showRemoveButton) {
                IconButton(
                    onClick = { showRemoveConfirmation = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove from collection",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            // Game information (bottom section)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                // Game name
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                // Game details row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Release date and external rating
                    Column {
                        game.releaseDate?.let { releaseDate ->
                            Text(
                                text = releaseDate.take(4), // Show only year
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        // External rating
                        if (game.rating > 0.0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StarBorder,
                                    contentDescription = "External rating",
                                    tint = Color.Yellow.copy(alpha = 0.7f),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "${(game.rating * 10).toInt() / 10.0}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    // Quick rating component
                    if (onQuickRating != null) {
                        QuickRating(
                            currentRating = gameWithUserData.userRating?.rating ?: 0,
                            onRatingChanged = onQuickRating,
                            showRatingDialog = showQuickRatingDialog,
                            onShowRatingDialog = { showQuickRatingDialog = true },
                            onHideRatingDialog = { showQuickRatingDialog = false }
                        )
                    }
                }
                
                // Platforms (if available)
                if (game.platforms.isNotEmpty()) {
                    Text(
                        text = game.platforms.take(2).joinToString(", ") { it.name } + 
                               if (game.platforms.size > 2) " +${game.platforms.size - 2}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                
                // Genres (if available)
                if (game.genres.isNotEmpty()) {
                    Text(
                        text = game.genres.take(2).joinToString(", ") { it.name } + 
                               if (game.genres.size > 2) " +${game.genres.size - 2}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
    
    // Remove confirmation dialog
    if (showRemoveConfirmation) {
        RemoveGameConfirmationDialog(
            gameName = game.name,
            onConfirm = {
                onRemove()
                showRemoveConfirmation = false
            },
            onDismiss = {
                showRemoveConfirmation = false
            }
        )
    }
}

/**
 * Confirmation dialog for removing a game from collection
 */
@Composable
private fun RemoveGameConfirmationDialog(
    gameName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Remove Game",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to remove \"$gameName\" from this collection?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            ExpressiveTextButton(
                onClick = onConfirm,
                contentDescription = "Confirm remove game from collection"
            ) {
                Text(
                    text = "Remove",
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            ExpressiveTextButton(
                onClick = onDismiss,
                contentDescription = "Cancel remove game action"
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Backward-compatible version of CollectionGameCard for existing code
 */
@Composable
fun CollectionGameCard(
    game: Game,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    showRemoveButton: Boolean = true
) {
    CollectionGameCard(
        gameWithUserData = GameWithUserData(game = game),
        onClick = onClick,
        onRemove = onRemove,
        modifier = modifier,
        showRemoveButton = showRemoveButton
    )
}

@Preview
@Composable
fun CollectionGameCardPreview() {
    CollectionGameCard(
        gameWithUserData = GameWithUserData(
            game = Game(
                id = 1,
                name = "Super Mario Bros.",
                imageUrl = "",
                platforms = listOf(
                    Platform(1, "Nintendo Switch", "nintendo-switch"),
                    Platform(2, "PC", "pc")
                ),
                genres = listOf(
                    Genre(1, "Platformer", "platformer"),
                    Genre(2, "Adventure", "adventure")
                ),
                rating = 4.5,
                releaseDate = "1985-09-13"
            ),
            userRating = UserRating(
                gameId = 1,
                rating = 5,
                createdAt = DateTimeUtils.getCurrentTimestamp(),
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            ),
            userReview = UserReview(
                gameId = 1,
                reviewText = "Amazing platformer! Brings back childhood memories.",
                createdAt = DateTimeUtils.getCurrentTimestamp(),
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            )
        ),
        onClick = {},
        onRemove = {}
    )
}

@Preview
@Composable
fun CollectionGameCardNoUserDataPreview() {
    CollectionGameCard(
        gameWithUserData = GameWithUserData(
            game = Game(
                id = 2,
                name = "The Legend of Zelda: Breath of the Wild",
                imageUrl = "",
                platforms = listOf(
                    Platform(1, "Nintendo Switch", "nintendo-switch")
                ),
                genres = listOf(
                    Genre(1, "Action", "action"),
                    Genre(2, "Adventure", "adventure"),
                    Genre(3, "RPG", "rpg")
                ),
                rating = 4.8,
                releaseDate = "2017-03-03"
            )
        ),
        onClick = {},
        onRemove = {},
        showRemoveButton = false
    )
}

@Preview
@Composable
fun CollectionGameCardUserRatingOnlyPreview() {
    CollectionGameCard(
        gameWithUserData = GameWithUserData(
            game = Game(
                id = 3,
                name = "Cyberpunk 2077",
                imageUrl = "",
                platforms = listOf(
                    Platform(1, "PC", "pc")
                ),
                genres = listOf(
                    Genre(1, "RPG", "rpg"),
                    Genre(2, "Action", "action")
                ),
                rating = 3.8,
                releaseDate = "2020-12-10"
            ),
            userRating = UserRating(
                gameId = 3,
                rating = 4,
                createdAt = DateTimeUtils.getCurrentTimestamp(),
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            )
        ),
        onClick = {},
        onRemove = {}
    )
}