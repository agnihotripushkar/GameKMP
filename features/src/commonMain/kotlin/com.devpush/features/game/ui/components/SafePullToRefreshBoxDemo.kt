@file:OptIn(ExperimentalMaterial3Api::class)

package com.devpush.features.game.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Demonstration of SafePullToRefreshBox usage as a drop-in replacement
 * for PullToRefreshBox in the CollectionDetailScreen scenario.
 * 
 * This shows how the SafePullToRefreshBox prevents the infinite constraint
 * crash that occurred in the original implementation.
 */

/**
 * Demo showing the problematic scenario that SafePullToRefreshBox fixes.
 * 
 * This demonstrates the exact scenario from CollectionDetailScreen where
 * PullToRefreshBox with nested LazyVerticalGrid would crash due to infinite
 * height constraints.
 */
@Composable
fun SafePullToRefreshBoxDemo_CollectionDetailScenario(
    collectionName: String = "My Collection",
    games: List<String> = listOf("Game 1", "Game 2", "Game 3", "Game 4", "Game 5"),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    // This is the SAFE version that prevents crashes
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        // This is the exact layout structure from CollectionDetailScreen
        // that was causing crashes with the original PullToRefreshBox
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Collection info header - fixed height content
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${games.size} games",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // Games grid - this would crash with original PullToRefreshBox
                // due to infinite height constraints, but SafePullToRefreshBox
                // automatically provides safe fallback behavior
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f) // This provides finite height constraints
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(games) { game ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = game,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Demo showing the UNSAFE version that would crash (for comparison).
 * 
 * This demonstrates what would happen with the original PullToRefreshBox
 * in the same scenario. This is commented out to prevent actual crashes.
 */
@Composable
fun SafePullToRefreshBoxDemo_UnsafeComparison(
    collectionName: String = "My Collection",
    games: List<String> = listOf("Game 1", "Game 2", "Game 3"),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    // NOTE: This would crash in the problematic constraint scenario
    // We use SafePullToRefreshBox here to prevent actual crashes in the demo
    
    /*
    // UNSAFE VERSION (commented out to prevent crashes):
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = rememberPullToRefreshState(),
        modifier = Modifier.fillMaxSize()
    ) {
        // This exact structure would crash when parent container
        // provides infinite height constraints
        Column(modifier = Modifier.fillMaxSize()) {
            // Header content
            Card(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = collectionName, modifier = Modifier.padding(16.dp))
            }
            
            // This LazyVerticalGrid would receive infinite height constraints
            // and crash with IllegalStateException
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                modifier = Modifier.fillMaxSize() // This causes the crash!
            ) {
                items(games) { game ->
                    Card { Text(text = game) }
                }
            }
        }
    }
    */
    
    // SAFE VERSION (what we actually use):
    SafePullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "$collectionName (Safe Version)",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            
            Box(modifier = Modifier.weight(1f)) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(games) { game ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = game)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Demo showing migration from PullToRefreshBox to SafePullToRefreshBox.
 * 
 * This demonstrates the simple migration process.
 */
@Composable
fun SafePullToRefreshBoxDemo_Migration(
    items: List<String> = listOf("Item 1", "Item 2", "Item 3"),
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Migration Demo",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        
        // Show the safe version
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "✅ Safe Version (SafePullToRefreshBox)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                SafePullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
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
        }
        
        // Show migration instructions
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Migration Steps:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "1. Replace PullToRefreshBox with SafePullToRefreshBox\n" +
                          "2. Remove manual constraint handling\n" +
                          "3. Test with various screen sizes\n" +
                          "4. Verify pull-to-refresh still works",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Demo showing constraint validation in action.
 */
@Composable
fun SafePullToRefreshBoxDemo_ConstraintValidation() {
    var validationResult by remember { mutableStateOf<String>("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Constraint Validation Demo",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Button(
            onClick = {
                // Simulate problematic constraints
                val problematicConstraints = androidx.compose.ui.unit.Constraints(
                    minWidth = 0,
                    maxWidth = 1080,
                    minHeight = 0,
                    maxHeight = androidx.compose.ui.unit.Constraints.Infinity
                )
                
                val result = validateScrollableConstraints(problematicConstraints)
                validationResult = buildString {
                    appendLine("Validation Result:")
                    appendLine("Is Valid: ${result.isValid}")
                    appendLine("Has Infinite Height: ${result.hasInfiniteHeight}")
                    appendLine("Has Infinite Width: ${result.hasInfiniteWidth}")
                    appendLine("\nRecommendations:")
                    result.recommendations.forEach { recommendation ->
                        appendLine("• $recommendation")
                    }
                }
            }
        ) {
            Text("Test Problematic Constraints")
        }
        
        Button(
            onClick = {
                // Simulate safe constraints
                val safeConstraints = androidx.compose.ui.unit.Constraints(
                    minWidth = 0,
                    maxWidth = 1080,
                    minHeight = 0,
                    maxHeight = 2340
                )
                
                val result = validateScrollableConstraints(safeConstraints)
                validationResult = buildString {
                    appendLine("Validation Result:")
                    appendLine("Is Valid: ${result.isValid}")
                    appendLine("Has Infinite Height: ${result.hasInfiniteHeight}")
                    appendLine("Has Infinite Width: ${result.hasInfiniteWidth}")
                    appendLine("\nRecommendations:")
                    result.recommendations.forEach { recommendation ->
                        appendLine("• $recommendation")
                    }
                }
            }
        ) {
            Text("Test Safe Constraints")
        }
        
        if (validationResult.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = validationResult,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}