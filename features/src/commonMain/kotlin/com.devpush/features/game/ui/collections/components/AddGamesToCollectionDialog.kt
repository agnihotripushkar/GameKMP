package com.devpush.features.game.ui.collections.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.devpush.features.game.domain.model.Game
import com.devpush.features.game.domain.model.Platform
import com.devpush.features.game.domain.model.Genre
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Dialog for selecting and adding multiple games to a collection with search and filtering capabilities.
 * 
 * @param availableGames List of games that can be added to the collection
 * @param onDismiss Callback when dialog is dismissed
 * @param onAddGames Callback when games are selected for addition with list of game IDs
 * @param modifier Modifier for styling
 * @param isLoading Whether the available games are currently loading
 */
@Composable
fun AddGamesToCollectionDialog(
    availableGames: List<Game>,
    onDismiss: () -> Unit,
    onAddGames: (List<Int>) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedGameIds by remember { mutableStateOf(setOf<Int>()) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Filter games based on search query
    val filteredGames by remember {
        derivedStateOf {
            if (searchQuery.isBlank()) {
                availableGames
            } else {
                availableGames.filter { game ->
                    game.name.contains(searchQuery, ignoreCase = true) ||
                    game.genres.any { it.name.contains(searchQuery, ignoreCase = true) } ||
                    game.platforms.any { it.name.contains(searchQuery, ignoreCase = true) }
                }
            }
        }
    }
    
    val hasSelectedGames = selectedGameIds.isNotEmpty()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Games",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (hasSelectedGames) {
                        Text(
                            text = "${selectedGameIds.size} game${if (selectedGameIds.size == 1) "" else "s"} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
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
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search games") },
                    placeholder = { Text("Search by name, genre, or platform") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else null,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                        }
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Games list
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Loading games...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    filteredGames.isEmpty() && searchQuery.isNotEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No games found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Try a different search term",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    availableGames.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No games available",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "All games are already in this collection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    
                    else -> {
                        LazyColumn(
                            modifier = Modifier.height(400.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(
                                items = filteredGames,
                                key = { game -> game.id }
                            ) { game ->
                                GameSelectionItem(
                                    game = game,
                                    isSelected = selectedGameIds.contains(game.id),
                                    onSelectionChange = { isSelected ->
                                        selectedGameIds = if (isSelected) {
                                            selectedGameIds + game.id
                                        } else {
                                            selectedGameIds - game.id
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                
                // Selection summary
                if (hasSelectedGames) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedGameIds.size} of ${availableGames.size} games selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        TextButton(
                            onClick = { selectedGameIds = emptySet() }
                        ) {
                            Text("Clear All")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onAddGames(selectedGameIds.toList())
                },
                enabled = hasSelectedGames
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Selected")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Individual game selection item with checkbox and game information
 */
@Composable
private fun GameSelectionItem(
    game: Game,
    isSelected: Boolean,
    onSelectionChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelectionChange(!isSelected) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Game image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = game.imageUrl,
                    contentDescription = game.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Game information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Release year and rating
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    game.releaseDate?.let { releaseDate ->
                        Text(
                            text = releaseDate.take(4),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    if (game.rating > 0.0 && game.releaseDate != null) {
                        Text(
                            text = " • ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                    
                    if (game.rating > 0.0) {
                        Text(
                            text = "★ ${String.format("%.1f", game.rating)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Platforms
                if (game.platforms.isNotEmpty()) {
                    Text(
                        text = game.platforms.take(3).joinToString(", ") { it.name } +
                               if (game.platforms.size > 3) " +${game.platforms.size - 3}" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Selection checkbox
            Checkbox(
                checked = isSelected,
                onCheckedChange = onSelectionChange
            )
        }
    }
}

@Preview
@Composable
fun AddGamesToCollectionDialogPreview() {
    val sampleGames = listOf(
        Game(
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
        Game(
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
        ),
        Game(
            id = 3,
            name = "Minecraft",
            imageUrl = "",
            platforms = listOf(
                Platform(1, "PC", "pc"),
                Platform(2, "PlayStation", "playstation"),
                Platform(3, "Xbox", "xbox")
            ),
            genres = listOf(
                Genre(1, "Sandbox", "sandbox"),
                Genre(2, "Survival", "survival")
            ),
            rating = 4.2,
            releaseDate = "2011-11-18"
        )
    )
    
    AddGamesToCollectionDialog(
        availableGames = sampleGames,
        onDismiss = {},
        onAddGames = {}
    )
}

@Preview
@Composable
fun AddGamesToCollectionDialogLoadingPreview() {
    AddGamesToCollectionDialog(
        availableGames = emptyList(),
        onDismiss = {},
        onAddGames = {},
        isLoading = true
    )
}

@Preview
@Composable
fun AddGamesToCollectionDialogEmptyPreview() {
    AddGamesToCollectionDialog(
        availableGames = emptyList(),
        onDismiss = {},
        onAddGames = {}
    )
}