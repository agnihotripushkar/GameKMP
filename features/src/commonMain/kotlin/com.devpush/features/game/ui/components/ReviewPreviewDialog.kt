package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.devpush.features.userRatingsReviews.domain.model.GameWithUserData
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.devpush.features.game.domain.model.Game
import com.devpush.features.userRatingsReviews.domain.model.UserRating
import com.devpush.features.userRatingsReviews.domain.model.UserReview
import com.devpush.features.common.utils.DateTimeUtils

/**
 * Dialog component for quickly previewing a user's review
 * @param gameWithUserData The game with user data containing the review
 * @param onDismiss Callback when the dialog is dismissed
 * @param onEditReview Callback when the edit button is clicked (optional)
 */
@Composable
fun ReviewPreviewDialog(
    gameWithUserData: GameWithUserData,
    onDismiss: () -> Unit,
    onEditReview: (() -> Unit)? = null
) {
    val userReview = gameWithUserData.userReview
    val userRating = gameWithUserData.userRating
    
    if (userReview == null) {
        onDismiss()
        return
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header with game name and rating
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = gameWithUserData.game.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // User rating if available
                        if (userRating != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                repeat(5) { index ->
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (index < userRating.rating) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.outline
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${userRating.rating}/5",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    // Edit button
                    if (onEditReview != null) {
                        IconButton(onClick = onEditReview) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit review",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Review text
                Text(
                    text = "Your Review",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = userReview.reviewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Review date
                Text(
                    text = "Reviewed on ${formatDate(userReview.createdAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (userReview.updatedAt != userReview.createdAt) {
                    Text(
                        text = "Last updated ${formatDate(userReview.updatedAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

/**
 * Formats a timestamp to a readable date string using platform-agnostic DateTimeUtils
 */
private fun formatDate(timestamp: Long): String {
    return DateTimeUtils.formatTimestamp(timestamp)
}

@Preview
@Composable
fun ReviewPreviewDialogPreview() {
    ReviewPreviewDialog(
        gameWithUserData = GameWithUserData(
            game = Game(
                id = 1,
                name = "The Legend of Zelda: Breath of the Wild",
                imageUrl = "",
                platforms = emptyList(),
                genres = emptyList(),
                rating = 4.8,
                releaseDate = "2017-03-03"
            ),
            userRating = UserRating(
                gameId = 1,
                rating = 5,
                createdAt = DateTimeUtils.getCurrentTimestamp() - 86400000, // 1 day ago
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            ),
            userReview = UserReview(
                gameId = 1,
                reviewText = "This game completely redefined what an open-world adventure could be. The freedom to explore, the physics-based puzzles, and the sheer beauty of Hyrule make this an unforgettable experience. Every mountain peak calls to be climbed, every shrine offers a unique challenge. A masterpiece that will be remembered for years to come.",
                createdAt = DateTimeUtils.getCurrentTimestamp() - 86400000,
                updatedAt = DateTimeUtils.getCurrentTimestamp()
            )
        ),
        onDismiss = {},
        onEditReview = {}
    )
}