package com.devpush.features.game.ui.collections.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.GameCollection
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Dialog for editing collection name and description with validation
 * 
 * @param collection The collection to edit
 * @param onDismiss Callback when dialog is dismissed
 * @param onUpdateCollection Callback when collection should be updated
 * @param isLoading Whether the update operation is in progress
 * @param error Error message to display, if any
 */
@Composable
fun EditCollectionDialog(
    collection: GameCollection,
    onDismiss: () -> Unit,
    onUpdateCollection: (name: String, description: String?) -> Unit,
    isLoading: Boolean = false,
    error: String? = null
) {
    var name by remember { mutableStateOf(collection.name) }
    var description by remember { mutableStateOf(collection.description ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Focus on name field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Validation functions
    fun validateName(input: String): String? {
        return when {
            input.isBlank() -> "Collection name cannot be empty"
            input.length < 2 -> "Collection name must be at least 2 characters"
            input.length > 50 -> "Collection name cannot exceed 50 characters"
            input.trim() != input -> "Collection name cannot start or end with spaces"
            else -> null
        }
    }
    
    fun validateDescription(input: String): String? {
        return when {
            input.length > 200 -> "Description cannot exceed 200 characters"
            else -> null
        }
    }
    
    // Update validation on text changes
    LaunchedEffect(name) {
        nameError = validateName(name)
    }
    
    LaunchedEffect(description) {
        descriptionError = validateDescription(description)
    }
    
    val isValid = nameError == null && descriptionError == null && name.isNotBlank()
    val hasChanges = name.trim() != collection.name || description.trim() != (collection.description ?: "")
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
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
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit collection",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    
                    Column {
                        Text(
                            text = "Edit Collection",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        if (collection.type.isDefault) {
                            Text(
                                text = "Default Collection",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                // Warning for default collections
                if (collection.type.isDefault) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(4.dp)
                            )
                            
                            Text(
                                text = "Editing default collections may affect the app experience. Consider carefully before making changes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    isError = nameError != null,
                    supportingText = nameError?.let { error ->
                        {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    enabled = !isLoading
                )
                
                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { error ->
                        {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } ?: {
                        Text(
                            text = "${description.length}/200 characters",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (isValid && hasChanges) {
                                onUpdateCollection(name.trim(), description.trim().takeIf { it.isNotEmpty() })
                            }
                        }
                    ),
                    maxLines = 3,
                    enabled = !isLoading
                )
                
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
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            onUpdateCollection(name.trim(), description.trim().takeIf { it.isNotEmpty() })
                        },
                        enabled = isValid && hasChanges && !isLoading
                    ) {
                        Text(if (isLoading) "Updating..." else "Update")
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun EditCollectionDialogPreview() {
    EditCollectionDialog(
        collection = GameCollection(
            id = "1",
            name = "My Custom Collection",
            type = CollectionType.CUSTOM,
            gameIds = listOf(1, 2, 3),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            description = "A collection of my favorite games"
        ),
        onDismiss = {},
        onUpdateCollection = { _, _ -> }
    )
}

@Preview
@Composable
fun EditCollectionDialogDefaultPreview() {
    EditCollectionDialog(
        collection = GameCollection(
            id = "2",
            name = "Wishlist",
            type = CollectionType.WISHLIST,
            gameIds = listOf(1, 2, 3, 4, 5),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        onDismiss = {},
        onUpdateCollection = { _, _ -> }
    )
}

@Preview
@Composable
fun EditCollectionDialogErrorPreview() {
    EditCollectionDialog(
        collection = GameCollection(
            id = "3",
            name = "Test Collection",
            type = CollectionType.CUSTOM,
            gameIds = emptyList(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        ),
        onDismiss = {},
        onUpdateCollection = { _, _ -> },
        error = "Collection name already exists"
    )
}