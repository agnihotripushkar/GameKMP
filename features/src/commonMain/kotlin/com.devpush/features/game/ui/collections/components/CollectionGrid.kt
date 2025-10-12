package com.devpush.features.game.ui.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.collections.CollectionError
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.usecase.CollectionWithCount
import com.devpush.features.game.ui.components.CollectionCard
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.devpush.features.game.domain.model.collections.CollectionType

/**
 * Responsive grid layout component for displaying game collections with loading states, 
 * empty state handling, and error displays.
 * 
 * @param collections List of collections to display
 * @param onCollectionClick Callback when a collection is clicked
 * @param onEditCollection Callback when a collection edit is requested
 * @param onDeleteCollection Callback when a collection delete is requested
 * @param isLoading Whether the collections are currently loading
 * @param error Error state if collection loading failed
 * @param modifier Modifier for styling
 * @param contentPadding Padding around the grid content
 */
@Composable
fun CollectionGrid(
    collections: List<CollectionWithCount>,
    onCollectionClick: (String) -> Unit,
    onEditCollection: (CollectionWithCount) -> Unit,
    onDeleteCollection: (CollectionWithCount) -> Unit,
    onViewCollectionDetails: ((CollectionWithCount) -> Unit)? = null,
    onShareCollection: ((CollectionWithCount) -> Unit)? = null,
    isLoading: Boolean = false,
    error: CollectionError? = null,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                LoadingState()
            }
            
            error != null -> {
                ErrorState(
                    error = error,
                    modifier = Modifier.padding(contentPadding)
                )
            }
            
            collections.isEmpty() -> {
                EmptyCollectionsState(
                    modifier = Modifier.padding(contentPadding)
                )
            }
            
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = contentPadding,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(
                        items = collections,
                        key = { collection -> collection.id }
                    ) { collection ->
                        CollectionCard(
                            collection = collection,
                            onClick = { onCollectionClick(collection.id) },
                            onEdit = { onEditCollection(collection) },
                            onDelete = { onDeleteCollection(collection) },
                            onViewDetails = onViewCollectionDetails?.let { callback -> { callback(collection) } },
                            onShare = onShareCollection?.let { callback -> { callback(collection) } }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Loading state component
 */
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading collections...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

/**
 * Empty state component when no collections exist
 */
@Composable
fun EmptyCollectionsState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Folder,
            contentDescription = "No collections",
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No Collections Yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Create your first collection to organize your games",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Suggestion to create collection
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add collection",
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap the + button to create a collection",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Error state component when collection loading fails
 */
@Composable
private fun ErrorState(
    error: CollectionError,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        Text(
            text = "Unable to Load Collections",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = getErrorMessage(error),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pull down to refresh",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Gets user-friendly error message for different error types
 */
private fun getErrorMessage(error: CollectionError): String {
    return when (error) {
        is CollectionError.DatabaseError -> 
            "There was a problem accessing your collections. Please try again."
        is CollectionError.CollectionNotFound -> 
            "Some collections could not be found. Please refresh to reload."
        is CollectionError.ValidationError -> 
            "There was a validation error: ${error.field}"
        is CollectionError.CollectionNameExists -> 
            "A collection with this name already exists."
        is CollectionError.DuplicateGameInCollection -> 
            "This game is already in the collection."
        else -> 
            "An error occurred: ${error.message}"
    }
}

@Preview
@Composable
fun CollectionGridPreview() {
    val sampleCollections = listOf(
        CollectionWithCount(
            collection = GameCollection(
                id = "1",
                name = "Wishlist",
                type = CollectionType.WISHLIST,
                gameIds = listOf(1, 2, 3, 4, 5),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 5
        ),
        CollectionWithCount(
            collection = GameCollection(
                id = "2",
                name = "Currently Playing",
                type = CollectionType.CURRENTLY_PLAYING,
                gameIds = listOf(6, 7),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 2
        ),
        CollectionWithCount(
            collection = GameCollection(
                id = "3",
                name = "Completed",
                type = CollectionType.COMPLETED,
                gameIds = listOf(8, 9, 10, 11, 12, 13),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 6
        ),
        CollectionWithCount(
            collection = GameCollection(
                id = "4",
                name = "Indie Favorites",
                type = CollectionType.CUSTOM,
                gameIds = listOf(14, 15, 16),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            gameCount = 3
        )
    )
    
    CollectionGrid(
        collections = sampleCollections,
        onCollectionClick = {},
        onEditCollection = {},
        onDeleteCollection = {}
    )
}

@Preview
@Composable
fun CollectionGridLoadingPreview() {
    CollectionGrid(
        collections = emptyList(),
        onCollectionClick = {},
        onEditCollection = {},
        onDeleteCollection = {},
        isLoading = true
    )
}

@Preview
@Composable
fun CollectionGridEmptyPreview() {
    CollectionGrid(
        collections = emptyList(),
        onCollectionClick = {},
        onEditCollection = {},
        onDeleteCollection = {}
    )
}

@Preview
@Composable
fun CollectionGridErrorPreview() {
    CollectionGrid(
        collections = emptyList(),
        onCollectionClick = {},
        onEditCollection = {},
        onDeleteCollection = {},
        error = CollectionError.DatabaseError
    )
}