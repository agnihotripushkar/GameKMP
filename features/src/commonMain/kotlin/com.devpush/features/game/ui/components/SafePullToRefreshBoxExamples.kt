@file:OptIn(ExperimentalMaterial3Api::class)

package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Usage examples for SafePullToRefreshBox component.
 * 
 * This file demonstrates various ways to use SafePullToRefreshBox
 * to prevent constraint-related crashes in scrollable components.
 */

/**
 * Example 1: Basic usage with LazyColumn
 * 
 * This is the most common use case - replacing PullToRefreshBox
 * with SafePullToRefreshBox for LazyColumn content.
 */
@Composable
fun SafePullToRefreshBoxExample_LazyColumn(
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Example 2: Usage with LazyVerticalGrid
 * 
 * This example shows how to use SafePullToRefreshBox with LazyVerticalGrid,
 * which is particularly prone to infinite constraint issues.
 */
@Composable
fun SafePullToRefreshBoxExample_LazyVerticalGrid(
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = item)
                    }
                }
            }
        }
    }
}

/**
 * Example 3: Usage with complex layout hierarchy
 * 
 * This example demonstrates using SafePullToRefreshBox in a complex
 * layout with header content and scrollable content.
 */
@Composable
fun SafePullToRefreshBoxExample_ComplexLayout(
    headerTitle: String,
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit
) {
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Fixed header content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = headerTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )
            }
            
            // Scrollable content - uses weight to consume remaining space
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Example 4: Usage with validation and debugging
 * 
 * This example shows how to use SafePullToRefreshBoxWithValidation
 * for enhanced debugging capabilities.
 */
@Composable
fun SafePullToRefreshBoxExample_WithValidation(
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    enableDebugLogging: Boolean = false
) {
    SafePullToRefreshBoxWithValidation(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        fallbackHeight = 500.dp, // Custom fallback height
        enableDebugLogging = enableDebugLogging
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = item,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Example 5: Migration from PullToRefreshBox
 * 
 * This example shows the before and after of migrating from
 * PullToRefreshBox to SafePullToRefreshBox.
 */
@Composable
fun SafePullToRefreshBoxExample_Migration(
    items: List<String>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    useSafeVersion: Boolean = true
) {
    if (useSafeVersion) {
        // AFTER: Safe version that prevents crashes
        SafePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // BEFORE: Original version that could crash with infinite constraints
        // Note: This is commented out to prevent actual crashes
        /*
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh,
            state = rememberPullToRefreshState(),
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
        */
        
        // Fallback to safe version
        SafePullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = onRefresh
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Example 6: Error handling and empty states
 * 
 * This example shows how to handle different states within SafePullToRefreshBox.
 */
@Composable
fun SafePullToRefreshBoxExample_StateHandling(
    items: List<String>,
    isLoading: Boolean,
    isRefreshing: Boolean,
    error: String?,
    onRefresh: () -> Unit,
    onRetry: () -> Unit
) {
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        when {
            isLoading && items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: $error",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = onRetry) {
                            Text("Retry")
                        }
                    }
                }
            }
            
            items.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No items available",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}