package com.devpush.features.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpush.features.game.ui.components.GameSearchBar
import com.devpush.features.game.ui.components.FilterPanel
import com.devpush.features.game.ui.components.ActiveFiltersChips
import com.devpush.features.game.ui.components.GameCard
import com.devpush.features.game.ui.components.AddToCollectionDialog
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit,
    onNavigateToCollections: () -> Unit = {}
) {
    val viewModel = koinViewModel<GameViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var showFilterPanel by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Show snackbar for filter errors
    LaunchedEffect(uiState.value.filterError) {
        uiState.value.filterError?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "An error occurred",
                actionLabel = "Retry"
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Games") },
                actions = {
                    // Collections button
                    IconButton(
                        onClick = onNavigateToCollections
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "View collections",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshGames() },
                        enabled = !uiState.value.isLoading && !uiState.value.isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh games",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Filter toggle button
                    IconButton(
                        onClick = { showFilterPanel = !showFilterPanel }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Toggle filters",
                            tint = if (uiState.value.searchFilterState.hasActiveFilters()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            GameSearchBar(
                query = uiState.value.searchFilterState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                onClearQuery = viewModel::clearSearch,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Filter Panel
            if (showFilterPanel) {
                FilterPanel(
                    availablePlatforms = uiState.value.availablePlatforms,
                    availableGenres = uiState.value.availableGenres,
                    selectedPlatforms = uiState.value.searchFilterState.selectedPlatforms,
                    selectedGenres = uiState.value.searchFilterState.selectedGenres,
                    minRating = uiState.value.searchFilterState.minRating,
                    onPlatformToggle = viewModel::togglePlatform,
                    onGenreToggle = viewModel::toggleGenre,
                    onRatingChange = viewModel::updateMinRating,
                    onClearFilters = viewModel::clearAllFilters,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Active Filters Chips
            if (uiState.value.searchFilterState.hasActiveFilters()) {
                ActiveFiltersChips(
                    searchQuery = uiState.value.searchFilterState.searchQuery,
                    selectedPlatforms = uiState.value.searchFilterState.selectedPlatforms,
                    selectedGenres = uiState.value.searchFilterState.selectedGenres,
                    minRating = uiState.value.searchFilterState.minRating,
                    onRemoveSearch = viewModel::clearSearch,
                    onRemovePlatform = viewModel::removePlatform,
                    onRemoveGenre = viewModel::removeGenre,
                    onRemoveRating = viewModel::clearRatingFilter,
                    onClearAll = viewModel::clearAllFilters,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Enhanced loading state for filters with animations
            AnimatedVisibility(
                visible = uiState.value.isFilterLoading,
                enter = fadeIn(animationSpec = tween(300)) + scaleIn(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(200)) + scaleOut(animationSpec = tween(200))
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Animated loading indicator
                            val infiniteTransition = rememberInfiniteTransition()
                            val rotation by infiniteTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = 360f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Restart
                                )
                            )
                            
                            Surface(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .rotate(rotation),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Filtering games...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Animated progress bar
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    }
                }
            }

            // Filter error display
            uiState.value.filterError?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Filter Error",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = error.message ?: "An error occurred",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (uiState.value.canRetry) {
                            OutlinedButton(
                                onClick = { viewModel.retryOperation() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }

            // Main content
            when {
                uiState.value.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Enhanced loading animation
                            val infiniteTransition = rememberInfiniteTransition()
                            val scale by infiniteTransition.animateFloat(
                                initialValue = 0.8f,
                                targetValue = 1.2f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(1000, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            val alpha by infiniteTransition.animateFloat(
                                initialValue = 0.5f,
                                targetValue = 1.0f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800, easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(48.dp)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                    },
                                strokeWidth = 4.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Loading games...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.alpha(alpha)
                            )
                        }
                    }
                }
                
                uiState.value.error?.isNotBlank() == true -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = "Oops! Something went wrong",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = uiState.value.error!!,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            if (uiState.value.canRetry) {
                                Button(
                                    onClick = { viewModel.retryOperation() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Try Again")
                                }
                            }
                        }
                    }
                }
                
                uiState.value.error.isNullOrBlank() && uiState.value.games.isEmpty() && !uiState.value.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "No games available",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Check your connection and try refreshing",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Button(
                                onClick = { viewModel.refreshGames() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Refresh")
                            }
                        }
                    }
                }
                
                uiState.value.filteredGames.isEmpty() && uiState.value.searchFilterState.hasActiveFilters() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No games found matching your criteria",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(16.dp)
                            )
                            Text(
                                text = "Try adjusting your search or filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    // Show filtered games
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.value.filteredGames) { gameItem ->
                            GameCard(
                                gameItem = gameItem,
                                onClick = { onClick(gameItem.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                searchQuery = uiState.value.searchFilterState.searchQuery,
                                onAddToCollection = { game -> viewModel.showAddToCollectionDialog(game) },
                                collectionsContainingGame = uiState.value.gameCollectionMap[gameItem.id] ?: emptyList()
                            )
                        }
                    }
                }
            }
        }
        
        // Add to Collection Dialog
        if (uiState.value.showAddToCollectionDialog && uiState.value.selectedGameForCollection != null) {
            AddToCollectionDialog(
                game = uiState.value.selectedGameForCollection!!,
                collections = uiState.value.collections,
                onDismiss = { viewModel.hideAddToCollectionDialog() },
                onAddToCollection = { collection -> viewModel.addGameToCollection(collection) }
            )
        }
    }
}

@Preview
@Composable
fun GameScreenPreview() {
    GameScreen(onClick = {}, onNavigateToCollections = {})
}