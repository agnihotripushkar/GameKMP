package com.devpush.features.bookmarklist.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devpush.features.bookmarklist.domain.collections.CollectionType
import com.devpush.features.bookmarklist.domain.collections.GameCollection
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

/**
 * Confirmation dialog for deleting collections with protection for default collections
 * 
 * @param collection The collection to delete
 * @param onConfirm Callback when deletion is confirmed
 * @param onDismiss Callback when dialog is dismissed
 * @param isDeleting Whether the deletion operation is in progress
 * @param error Error message to display, if any
 */
@Composable
fun DeleteCollectionConfirmationDialog(
    collection: GameCollection,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDeleting: Boolean = false,
    error: String? = null
) {
    val isDefaultCollection = collection.type.isDefault
    val gameCount = collection.getGameCount()
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isDeleting,
            dismissOnClickOutside = !isDeleting,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with warning icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isDefaultCollection) Icons.Default.Warning else Icons.Default.Delete,
                        contentDescription = if (isDefaultCollection) "Warning" else "Delete",
                        tint = if (isDefaultCollection) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column {
                        Text(
                            text = if (isDefaultCollection) "Cannot Delete Collection" else "Delete Collection?",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (isDefaultCollection) {
                            Text(
                                text = "Default Collection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                
                // Collection details
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = collection.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "${gameCount} ${if (gameCount == 1) "game" else "games"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        if (!collection.description.isNullOrBlank()) {
                            Text(
                                text = collection.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        
                        Text(
                            text = "Type: ${collection.type.displayName}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Warning or confirmation message
                if (isDefaultCollection) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Default collections cannot be deleted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Text(
                                text = "Default collections (Wishlist, Currently Playing, Completed) are essential for the app's functionality and cannot be removed.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else {
                    Text(
                        text = if (gameCount > 0) {
                            "This will permanently delete the collection and remove all ${gameCount} ${if (gameCount == 1) "game" else "games"} from it. The games themselves will not be deleted from your library."
                        } else {
                            "This will permanently delete the empty collection."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    if (gameCount > 0) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️ This action cannot be undone",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            )
                        }
                    }
                }
                
                // Error message
                error?.let { errorMessage ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveOutlinedButton(
                        onClick = onDismiss,
                        enabled = !isDeleting,
                        contentDescription = if (isDefaultCollection) "Acknowledge protection message" else "Cancel delete collection"
                    ) {
                        Text(if (isDefaultCollection) "OK" else "Cancel")
                    }
                    
                    if (!isDefaultCollection) {
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Button(
                            onClick = onConfirm,
                            enabled = !isDeleting,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            if (isDeleting) {
                                Text("Deleting...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Preview
@Composable
fun DeleteCollectionConfirmationDialogCustomPreview() {
    DeleteCollectionConfirmationDialog(
        collection = GameCollection(
            id = "1",
            name = "My Custom Collection",
            type = CollectionType.CUSTOM,
            gameIds = listOf(1, 2, 3, 4, 5),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            description = "A collection of my favorite indie games"
        ),
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview
@Composable
fun DeleteCollectionConfirmationDialogDefaultPreview() {
    DeleteCollectionConfirmationDialog(
        collection = GameCollection(
            id = "2",
            name = "Wishlist",
            type = CollectionType.WISHLIST,
            gameIds = listOf(1, 2, 3),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview
@Composable
fun DeleteCollectionConfirmationDialogEmptyPreview() {
    DeleteCollectionConfirmationDialog(
        collection = GameCollection(
            id = "3",
            name = "Empty Collection",
            type = CollectionType.CUSTOM,
            gameIds = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        onConfirm = {},
        onDismiss = {}
    )
}

@Preview
@Composable
fun DeleteCollectionConfirmationDialogErrorPreview() {
    DeleteCollectionConfirmationDialog(
        collection = GameCollection(
            id = "4",
            name = "Test Collection",
            type = CollectionType.CUSTOM,
            gameIds = listOf(1, 2),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        onConfirm = {},
        onDismiss = {},
        error = "Failed to delete collection. Please try again."
    )
}