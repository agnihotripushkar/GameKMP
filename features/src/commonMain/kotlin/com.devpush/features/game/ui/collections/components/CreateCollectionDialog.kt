package com.devpush.features.game.ui.collections.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.devpush.features.game.domain.model.collections.CollectionType
import com.devpush.features.game.domain.model.collections.GameCollection
import com.devpush.features.game.domain.model.collections.ValidationResult
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * Dialog for creating new collections with name, description input and collection type selection.
 * 
 * @param onDismiss Callback when dialog is dismissed
 * @param onCreateCollection Callback when collection creation is confirmed with name and type
 * @param modifier Modifier for styling
 * @param existingCollectionNames List of existing collection names for validation
 */
@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreateCollection: (name: String, type: CollectionType) -> Unit,
    modifier: Modifier = Modifier,
    existingCollectionNames: List<String> = emptyList()
) {
    var collectionName by remember { mutableStateOf("") }
    var collectionDescription by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(CollectionType.CUSTOM) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Focus on name field when dialog opens
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    // Validate name in real-time
    LaunchedEffect(collectionName) {
        nameError = validateCollectionName(collectionName, existingCollectionNames)
    }
    
    // Validate description in real-time
    LaunchedEffect(collectionDescription) {
        descriptionError = validateCollectionDescription(collectionDescription)
    }
    
    val isValid = nameError == null && collectionName.isNotBlank()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Collection",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
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
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Collection name input
                OutlinedTextField(
                    value = collectionName,
                    onValueChange = { collectionName = it },
                    label = { Text("Collection Name") },
                    placeholder = { Text("Enter collection name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    isError = nameError != null,
                    supportingText = nameError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true
                )
                
                // Collection description input (optional)
                OutlinedTextField(
                    value = collectionDescription,
                    onValueChange = { collectionDescription = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Enter collection description") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = descriptionError != null,
                    supportingText = descriptionError?.let { error ->
                        { Text(text = error, color = MaterialTheme.colorScheme.error) }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (isValid) {
                                onCreateCollection(
                                    collectionName.trim(),
                                    selectedType
                                )
                            }
                        }
                    ),
                    maxLines = 3
                )
                
                // Collection type selection
                Text(
                    text = "Collection Type",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Custom collection type (default selection)
                    CollectionTypeOption(
                        type = CollectionType.CUSTOM,
                        isSelected = selectedType == CollectionType.CUSTOM,
                        onClick = { selectedType = CollectionType.CUSTOM },
                        customName = collectionName.takeIf { it.isNotBlank() }
                    )
                    
                    // Default collection types (only if name matches)
                    CollectionType.getDefaultTypes().forEach { type ->
                        if (type.displayName.equals(collectionName.trim(), ignoreCase = true)) {
                            CollectionTypeOption(
                                type = type,
                                isSelected = selectedType == type,
                                onClick = { selectedType = type }
                            )
                        }
                    }
                }
                
                // Helper text for collection types
                Text(
                    text = when (selectedType) {
                        CollectionType.CUSTOM -> "Create a custom collection with your own name"
                        else -> selectedType.description
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreateCollection(
                        collectionName.trim(),
                        selectedType
                    )
                },
                enabled = isValid
            ) {
                Text("Create")
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
 * Individual collection type selection option
 */
@Composable
private fun CollectionTypeOption(
    type: CollectionType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    customName: String? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Collection type icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            color = getCollectionTypeColor(type).copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionTypeIcon(type),
                        contentDescription = type.displayName,
                        tint = getCollectionTypeColor(type),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (type == CollectionType.CUSTOM && customName != null) {
                            customName
                        } else {
                            type.displayName
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (type == CollectionType.CUSTOM) {
                            "Custom collection"
                        } else {
                            "Default collection"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Selection indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    }
}

/**
 * Validates collection name
 */
private fun validateCollectionName(name: String, existingNames: List<String>): String? {
    val trimmedName = name.trim()
    
    return when {
        trimmedName.isBlank() -> null // Don't show error for empty field initially
        trimmedName.length < 2 -> "Collection name must be at least 2 characters"
        trimmedName.length > 50 -> "Collection name cannot exceed 50 characters"
        existingNames.any { it.equals(trimmedName, ignoreCase = true) } -> 
            "A collection with this name already exists"
        !trimmedName.matches(Regex("^[a-zA-Z0-9\\s\\-_'\".,!?()]+$")) -> 
            "Collection name contains invalid characters"
        else -> null
    }
}

/**
 * Validates collection description
 */
private fun validateCollectionDescription(description: String): String? {
    return when {
        description.length > 200 -> "Description cannot exceed 200 characters"
        else -> null
    }
}

/**
 * Gets the appropriate icon for a collection type
 */
@Composable
private fun getCollectionTypeIcon(type: CollectionType): ImageVector {
    return when (type) {
        CollectionType.WISHLIST -> Icons.Default.Favorite
        CollectionType.CURRENTLY_PLAYING -> Icons.Default.PlayArrow
        CollectionType.COMPLETED -> Icons.Default.Star
        CollectionType.CUSTOM -> Icons.Default.VideoLibrary
    }
}

/**
 * Gets the appropriate color for a collection type
 */
@Composable
private fun getCollectionTypeColor(type: CollectionType): Color {
    return when (type) {
        CollectionType.WISHLIST -> MaterialTheme.colorScheme.primary
        CollectionType.CURRENTLY_PLAYING -> MaterialTheme.colorScheme.secondary
        CollectionType.COMPLETED -> MaterialTheme.colorScheme.tertiary
        CollectionType.CUSTOM -> MaterialTheme.colorScheme.outline
    }
}

@Preview
@Composable
fun CreateCollectionDialogPreview() {
    CreateCollectionDialog(
        onDismiss = {},
        onCreateCollection = { _, _ -> },
        existingCollectionNames = listOf("Wishlist", "Currently Playing", "Completed")
    )
}

@Preview
@Composable
fun CreateCollectionDialogWithDefaultTypePreview() {
    CreateCollectionDialog(
        onDismiss = {},
        onCreateCollection = { _, _ -> },
        existingCollectionNames = emptyList()
    )
}