package com.devpush.features.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import kmp.features.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.devpush.features.game.domain.model.Game
import kmp.features.generated.resources.games
import kmp.features.generated.resources.something_went_wrong
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit
) {
    val viewModel = koinViewModel<GameViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.games)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer, // Or Color.White if you prefer
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->

        if (uiState.value.isLoading) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.value.error?.isNotBlank() == true) {
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(uiState.value.error!!, color = MaterialTheme.colorScheme.error)
            }
        }
        else if (uiState.value.error.isNullOrBlank() && uiState.value.games.isEmpty()){
            Box(
                Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(Res.string.something_went_wrong), color = MaterialTheme.colorScheme.error)
            }
        }
        else {
            uiState.value.games.let { data ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = paddingValues // Apply padding from Scaffold here
                ) {
                    items(data) { gameItem ->
                        GameCard(
                            gameItem = gameItem,
                            onClick = { onClick(gameItem.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(
    gameItem: Game, // Assuming GameUiModel is the type for your game items
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
                contentDescription = gameItem.name, // Good practice to have content description
                modifier = Modifier.fillMaxSize(), // Adjusted to fill the card
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color.Black.copy(alpha = 0.5f), // Semi-transparent background for better text visibility
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp) // Adjusted padding
            ) {
                Text(
                    text = gameItem.name,
                    style = MaterialTheme.typography.titleMedium, // Using M3 typography
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
