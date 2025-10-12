package com.devpush.features.game.ui.collections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devpush.features.game.ui.collections.components.CollectionGrid
import com.devpush.features.game.ui.collections.components.CreateCollectionDialog
import com.devpush.features.game.ui.collections.components.EditCollectionDialog
import com.devpush.features.game.ui.collections.components.DeleteCollectionConfirmationDialog
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    modifier: Modifier = Modifier,
    onCollectionClick: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val viewModel = koinViewModel<CollectionsViewModel>()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    var showCreateDialog by remember { mutableStateOf(false) }

    // Show snackbar for errors
    LaunchedEffect(uiState.value.error) {
        uiState.value.error?.let { error ->
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
                title = { Text(text = "Collections") },
                actions = {
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refreshCollections() },
                        enabled = !uiState.value.isLoading && !uiState.value.isRefreshing
                    ) {
                        val infiniteTransition = rememberInfiniteTransition()
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = if (uiState.value.isRefreshing) 360f else 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            )
                        )
                        
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh collections",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.rotate(rotation)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new collection"
                )
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.value.isRefreshing,
            onRefresh = { viewModel.refreshCollections() },
            state = pullToRefreshState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
                            
                            Text(
                                text = "Loading collections...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
                
                uiState.value.error != null -> {
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
                                text = uiState.value.error!!.message ?: "An error occurred",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
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
                
                uiState.value.collections.isEmpty() && !uiState.value.isLoading -> {
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
                                text = "No collections yet",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Create your first collection to organize your games",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Button(
                                onClick = { showCreateDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Create Collection")
                            }
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            CollectionGrid(
                                collections = uiState.value.collections,
                                onCollectionClick = onCollectionClick,
                                onEditCollection = { collection ->
                                    viewModel.startEditingCollection(collection.collection)
                                },
                                onDeleteCollection = { collection ->
                                    viewModel.deleteCollection(collection.id)
                                },
                                onViewCollectionDetails = { collection ->
                                    // Navigate to collection details
                                    onCollectionClick(collection.id)
                                },
                                onShareCollection = { collection ->
                                    // TODO: Implement sharing functionality
                                    // For now, this could show a snackbar or dialog
                                    viewModel.shareCollection(collection.collection)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create Collection Dialog
    AnimatedVisibility(
        visible = showCreateDialog,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreateCollection = { name, type ->
                // For custom collections, we'll use the name as description if needed
                viewModel.createCollection(name, null)
                showCreateDialog = false
            }
        )
    }

    // Edit Collection Dialog
    uiState.value.editingCollection?.let { collection ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            EditCollectionDialog(
                collection = collection,
                onDismiss = { 
                    viewModel.stopEditingCollection()
                },
                onUpdateCollection = { name, description ->
                    viewModel.updateCollection(collection.id, name, description)
                },
                isLoading = uiState.value.isUpdating,
                error = uiState.value.updateError
            )
        }
    }

    // Delete Collection Confirmation Dialog
    uiState.value.collectionToDelete?.let { collection ->
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            DeleteCollectionConfirmationDialog(
                collection = collection,
                onConfirm = { 
                    viewModel.confirmDeleteCollection()
                },
                onDismiss = { 
                    viewModel.hideDeleteConfirmation()
                },
                isDeleting = uiState.value.isDeleting,
                error = uiState.value.deleteError
            )
        }
    }
}

@Preview
@Composable
fun CollectionsScreenPreview() {
    CollectionsScreen()
}