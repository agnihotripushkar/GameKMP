package com.devpush.features.userRatingsReviews.ui.components

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import com.devpush.features.ui.components.ExpressiveOutlinedTextField
import androidx.compose.material3.Text
import com.devpush.features.ui.components.ExpressiveTextButton
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

/**
 * A dialog component for writing and editing user reviews.
 * 
 * @param isVisible Whether the dialog is visible
 * @param initialReviewText Initial text for editing (empty for new review)
 * @param onDismiss Callback when dialog is dismissed
 * @param onSave Callback when review is saved with the review text
 * @param modifier Modifier for the component
 * @param maxCharacters Maximum characters allowed (default 1000)
 * @param isEditing Whether this is editing an existing review
 */
@Composable
fun ReviewDialog(
    isVisible: Boolean,
    initialReviewText: String = "",
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxCharacters: Int = 1000,
    isEditing: Boolean = false
) {
    if (!isVisible) return
    
    var reviewText by remember(initialReviewText) { mutableStateOf(initialReviewText) }
    var hasError by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Validation
    val isTextValid = reviewText.isNotBlank() && reviewText.length <= maxCharacters
    val characterCount = reviewText.length
    val isOverLimit = characterCount > maxCharacters
    
    LaunchedEffect(isVisible) {
        if (isVisible) {
            focusRequester.requestFocus()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.semantics {
            contentDescription = if (isEditing) "Edit review dialog" else "Write review dialog"
        },
        title = {
            Text(
                text = if (isEditing) "Edit Review" else "Write Review",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                ExpressiveOutlinedTextField(
                    value = reviewText,
                    onValueChange = { newText ->
                        reviewText = newText
                        hasError = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .semantics {
                            contentDescription = "Review text input. $characterCount of $maxCharacters characters used."
                        },
                    contentDescription = "Review text input field",
                    label = { Text("Your review") },
                    placeholder = { Text("Share your thoughts about this game...") },
                    minLines = 4,
                    maxLines = 8,
                    isError = hasError || isOverLimit,
                    supportingText = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (hasError) {
                                Text(
                                    text = "Review cannot be empty",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else if (isOverLimit) {
                                Text(
                                    text = "Review is too long",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            } else {
                                Spacer(modifier = Modifier.width(1.dp))
                            }
                            
                            Text(
                                text = "$characterCount/$maxCharacters",
                                color = if (isOverLimit) {
                                    MaterialTheme.colorScheme.error
                                } else if (characterCount > maxCharacters * 0.9) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Default
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            ExpressiveTextButton(
                onClick = {
                    if (reviewText.isBlank()) {
                        hasError = true
                    } else if (isTextValid) {
                        onSave(reviewText.trim())
                    }
                },
                enabled = isTextValid,
                contentDescription = if (isEditing) "Update review" else "Save review"
            ) {
                Text(if (isEditing) "Update" else "Save")
            }
        },
        dismissButton = {
            ExpressiveTextButton(
                onClick = onDismiss,
                contentDescription = "Cancel review dialog"
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}