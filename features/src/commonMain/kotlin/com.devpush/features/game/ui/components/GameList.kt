package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.Game
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Component for displaying a list of games with search result highlighting.
 * 
 * @param games List of games to display
 * @param onGameClick Callback when a game is clicked
 * @param modifier Modifier for styling
 * @param searchQuery Optional search query for highlighting matches
 * @param contentPadding Padding for the lazy column content
 */
@Composable
fun GameList(
    games: List<Game>,
    onGameClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = games,
            key = { game -> game.id }
        ) { game ->
            GameCard(
                gameItem = game,
                onClick = { onGameClick(game.id) },
                searchQuery = searchQuery,
                modifier = Modifier.fillParentMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun GameListPreview() {
    val sampleGames = listOf(
        Game(
            id = 1,
            name = "Super Mario Bros.",
            imageUrl = "",
            platforms = emptyList(),
            genres = emptyList(),
            rating = 4.5,
            releaseDate = "1985-09-13"
        ),
        Game(
            id = 2,
            name = "The Legend of Zelda",
            imageUrl = "",
            platforms = emptyList(),
            genres = emptyList(),
            rating = 4.8,
            releaseDate = "1986-02-21"
        )
    )
    
    GameList(
        games = sampleGames,
        onGameClick = {},
        searchQuery = "Mario"
    )
}