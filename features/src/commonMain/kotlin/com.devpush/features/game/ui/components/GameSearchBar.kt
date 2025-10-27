package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A search bar component for searching games with real-time updates and debouncing.
 * 
 * @param query The current search query
 * @param onQueryChange Callback when the search query changes (debounced)
 * @param onClearQuery Callback when the clear button is pressed
 * @param modifier Modifier for styling
 * @param placeholder Placeholder text for the search field
 * @param debounceTimeMs Debounce time in milliseconds (default 300ms)
 */
@Composable
fun GameSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search games...",
    debounceTimeMs: Long = 300L
) {
    var localQuery by remember(query) { mutableStateOf(query) }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Debounce the search query
    LaunchedEffect(localQuery) {
        if (localQuery != query) {
            delay(debounceTimeMs)
            onQueryChange(localQuery)
        }
    }
    
    ExpressiveOutlinedTextField(
        value = localQuery,
        onValueChange = { newValue ->
            localQuery = newValue
        },
        modifier = modifier.fillMaxWidth(),
        contentDescription = "Search games input field",
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search games",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = {
            if (localQuery.isNotEmpty()) {
                IconButton(
                    onClick = {
                        localQuery = ""
                        onClearQuery()
                        keyboardController?.hide()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
            }
        ),
        shape = MaterialTheme.shapes.medium
    )
}

@Preview
@Composable
fun GameSearchBarPreview() {
    GameSearchBar(
        query = "",
        onQueryChange = {},
        onClearQuery = {},
        modifier = Modifier.padding(16.dp)
    )
}

@Preview
@Composable
fun GameSearchBarWithTextPreview() {
    GameSearchBar(
        query = "Super Mario",
        onQueryChange = {},
        onClearQuery = {},
        modifier = Modifier.padding(16.dp)
    )
}