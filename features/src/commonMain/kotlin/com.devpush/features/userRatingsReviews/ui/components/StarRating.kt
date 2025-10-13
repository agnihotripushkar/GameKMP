package com.devpush.features.userRatingsReviews.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A star rating component that supports both read-only and interactive modes.
 * 
 * @param rating Current rating value (1-5)
 * @param onRatingChanged Callback when rating changes (null for read-only mode)
 * @param modifier Modifier for the component
 * @param maxRating Maximum rating value (default 5)
 * @param starSize Size of each star icon
 * @param starColor Color of filled stars
 * @param emptyStarColor Color of empty stars
 * @param contentDescription Accessibility description
 */
@Composable
fun StarRating(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 24.dp,
    starColor: Color = MaterialTheme.colorScheme.primary,
    emptyStarColor: Color = MaterialTheme.colorScheme.outline,
    contentDescription: String? = null
) {
    // Validate rating bounds
    val validRating = rating.coerceIn(0, maxRating)
    
    // Track hover state for interactive mode
    var hoveredRating by remember { mutableStateOf(0) }
    val isInteractive = onRatingChanged != null
    
    // Determine which rating to display (hovered or actual)
    val displayRating = if (isInteractive && hoveredRating > 0) hoveredRating else validRating
    
    Row(
        modifier = modifier.semantics {
            if (contentDescription != null) {
                this.contentDescription = contentDescription
            } else {
                this.contentDescription = if (isInteractive) {
                    "Rating: $validRating out of $maxRating stars. Tap to change rating."
                } else {
                    "Rating: $validRating out of $maxRating stars"
                }
            }
            if (isInteractive) {
                role = Role.Button
            }
        },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(maxRating) { index ->
            val starIndex = index + 1
            val isFilled = starIndex <= displayRating
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null, // Handled by parent semantics
                modifier = Modifier
                    .size(starSize)
                    .then(
                        if (isInteractive) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onRatingChanged?.invoke(starIndex)
                            }
                        } else {
                            Modifier
                        }
                    ),
                tint = if (isFilled) starColor else emptyStarColor
            )
        }
    }
}

/**
 * Compact version of StarRating for use in small spaces like game cards
 */
@Composable
fun CompactStarRating(
    rating: Int,
    modifier: Modifier = Modifier,
    maxRating: Int = 5,
    starSize: Dp = 16.dp,
    starColor: Color = MaterialTheme.colorScheme.primary,
    emptyStarColor: Color = MaterialTheme.colorScheme.outline
) {
    StarRating(
        rating = rating,
        onRatingChanged = null,
        modifier = modifier,
        maxRating = maxRating,
        starSize = starSize,
        starColor = starColor,
        emptyStarColor = emptyStarColor,
        contentDescription = "Rating: $rating out of $maxRating stars"
    )
}