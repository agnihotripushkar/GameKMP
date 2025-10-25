package com.devpush.kmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.devpush.kmp.ui.utils.ConstraintLogger
import org.jetbrains.compose.ui.tooling.preview.Preview

/**
 * A safe container for scrollable components that ensures proper height constraints.
 * 
 * This component wraps scrollable content and provides finite height constraints
 * to prevent infinite constraint crashes. It automatically detects when infinite
 * constraints are provided and applies safe fallback behavior.
 * 
 * @param modifier The modifier to be applied to the container
 * @param fallbackHeight The height to use when infinite constraints are detected
 * @param enableLogging Whether to log constraint validation information (debug builds only)
 * @param content The scrollable content to be wrapped
 */
@Composable
fun ConstrainedScrollableContainer(
    modifier: Modifier = Modifier,
    fallbackHeight: androidx.compose.ui.unit.Dp = 400.dp,
    enableLogging: Boolean = true,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val currentConstraints = constraints
        
        if (enableLogging) {
            ConstraintLogger.logConstraints(
                componentName = "ConstrainedScrollableContainer",
                constraints = currentConstraints
            )
        }
        
        when {
            maxHeight == androidx.compose.ui.unit.Dp.Infinity -> {
                if (enableLogging) {
                    ConstraintLogger.logInfiniteConstraint(
                        componentName = "ConstrainedScrollableContainer",
                        dimension = "height",
                        fallbackValue = fallbackHeight.toString()
                    )
                }
                
                // Apply fallback height when infinite constraints are detected
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(fallbackHeight)
                ) {
                    content()
                }
            }
            maxHeight == 0.dp -> {
                if (enableLogging) {
                    ConstraintLogger.logZeroConstraint(
                        componentName = "ConstrainedScrollableContainer",
                        dimension = "height"
                    )
                }
                
                // Handle zero height constraint
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(fallbackHeight)
                ) {
                    content()
                }
            }
            else -> {
                // Normal case - constraints are finite and valid
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentHeight(unbounded = false)
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * A variant of ConstrainedScrollableContainer that can be used within Column/Row layouts.
 * This version doesn't use weight modifier directly but can be combined with weight in the parent scope.
 * 
 * @param modifier The modifier to be applied to the container
 * @param fallbackHeight The height to use when infinite constraints are detected
 * @param enableLogging Whether to log constraint validation information
 * @param content The scrollable content to be wrapped
 */
@Composable
fun FlexibleConstrainedScrollableContainer(
    modifier: Modifier = Modifier,
    fallbackHeight: androidx.compose.ui.unit.Dp = 400.dp,
    enableLogging: Boolean = true,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val currentConstraints = constraints
        
        if (enableLogging) {
            ConstraintLogger.logConstraints(
                componentName = "FlexibleConstrainedScrollableContainer",
                constraints = currentConstraints
            )
        }
        
        when {
            maxHeight == androidx.compose.ui.unit.Dp.Infinity -> {
                if (enableLogging) {
                    ConstraintLogger.logInfiniteConstraint(
                        componentName = "FlexibleConstrainedScrollableContainer",
                        dimension = "height",
                        fallbackValue = fallbackHeight.toString()
                    )
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .height(fallbackHeight)
                ) {
                    content()
                }
            }
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

@Preview
@Composable
fun ConstrainedScrollableContainerPreview() {
    ConstrainedScrollableContainer(
        fallbackHeight = 300.dp,
        enableLogging = false
    ) {
        LazyColumn {
            items(20) { index ->
                Text(
                    text = "Item $index",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Preview
@Composable
fun FlexibleConstrainedScrollableContainerPreview() {
    Column {
        Text(
            text = "Header",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )
        
        FlexibleConstrainedScrollableContainer(
            fallbackHeight = 200.dp,
            enableLogging = false
        ) {
            LazyColumn {
                items(15) { index ->
                    Text(
                        text = "Flexible Item $index",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}