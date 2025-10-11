package com.devpush.features.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.devpush.features.game.domain.model.Game
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.util.Locale

/**
 * Enhanced GameCard component with search result highlighting support.
 * 
 * @param gameItem The game to display
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for styling
 * @param searchQuery Optional search query for highlighting matches in the game name
 */
@Composable
fun GameCard(
    gameItem: Game,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
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