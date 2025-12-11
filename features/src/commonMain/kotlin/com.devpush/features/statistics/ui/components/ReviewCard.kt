package com.devpush.features.statistics.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpush.features.statistics.domain.model.UserReview
import com.devpush.features.common.utils.DateTimeUtils
import com.devpush.features.common.preview.PreviewData
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A card component for displaying user reviews with edit and delete options.
 * 
 * @param review The user review to display
 * @param onEditClick Callback when edit button is clicked
 * @param onDeleteClick Callback when delete button is clicked
 * @param modifier Modifier for the component
 * @param maxLines Maximum lines to display for review text (null for unlimited)
 */
@Composable
fun ReviewCard(
    review: UserReview,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Review: ${review.reviewText}. Created on ${DateTimeUtils.formatTimestamp(review.createdAt)}"
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with timestamp and actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeUtils.formatTimestamp(review.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Edit review"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.semantics {
                            contentDescription = "Delete review"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Review text
            Text(
                text = review.reviewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = maxLines ?: Int.MAX_VALUE,
                overflow = if (maxLines != null) TextOverflow.Ellipsis else TextOverflow.Clip
            )
            
            // Show update timestamp if different from created
            if (review.updatedAt != review.createdAt) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Updated ${DateTimeUtils.formatTimestamp(review.updatedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Compact version of ReviewCard for use in smaller spaces
 */
@Composable
fun CompactReviewCard(
    review: UserReview,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ReviewCard(
        review = review,
        onEditClick = onEditClick,
        onDeleteClick = onDeleteClick,
        modifier = modifier,
        maxLines = 3
    )
}

@Preview
@Composable
fun ReviewCardPreview() {
    ReviewCard(
        review = PreviewData.sampleReview1,
        onEditClick = {},
        onDeleteClick = {}
    )
}

@Preview
@Composable
fun ReviewCardShortPreview() {
    ReviewCard(
        review = PreviewData.sampleReviewShort,
        onEditClick = {},
        onDeleteClick = {}
    )
}

@Preview
@Composable
fun CompactReviewCardPreview() {
    CompactReviewCard(
        review = PreviewData.sampleReview1,
        onEditClick = {},
        onDeleteClick = {}
    )
}

