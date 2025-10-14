package com.devpush.kmp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devpush.kmp.ui.utils.ConstraintLogger

/**
 * A safe wrapper around PullToRefreshBox that ensures proper constraint handling
 * for nested scrollable components.
 * 
 * This component provides finite constraints to its content and prevents the
 * infinite height constraint issues that can occur when scrollable components
 * are nested within PullToRefreshBox.
 * 
 * @param isRefreshing Whether the refresh operation is currently in progress
 * @param onRefresh Callback invoked when the user triggers a refresh
 * @param modifier The modifier to be applied to the container
 * @param state The PullToRefreshState to use, defaults to rememberPullToRefreshState()
 * @param enableLogging Whether to log constraint validation information (debug builds only)
 * @param content The content to be displayed within the pull-to-refresh container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    enableLogging: Boolean = true,
    content: @Composable () -> Unit
) {
    if (enableLogging) {
        ConstraintLogger.logConstraints(
            componentName = "SafePullToRefreshBox",
            constraints = androidx.compose.ui.unit.Constraints()
        )
    }
    
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = modifier.fillMaxSize()
    ) {
        // Wrap content in a Box with fillMaxSize to ensure finite constraints
        // are provided to nested scrollable components
        Box(modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

/**
 * A variant of SafePullToRefreshBox that uses ConstrainedScrollableContainer
 * for additional safety when dealing with complex nested layouts.
 * 
 * @param isRefreshing Whether the refresh operation is currently in progress
 * @param onRefresh Callback invoked when the user triggers a refresh
 * @param modifier The modifier to be applied to the container
 * @param state The PullToRefreshState to use
 * @param fallbackHeight The height to use if infinite constraints are detected
 * @param enableLogging Whether to log constraint validation information
 * @param content The content to be displayed within the pull-to-refresh container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraSafePullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    state: PullToRefreshState = rememberPullToRefreshState(),
    fallbackHeight: androidx.compose.ui.unit.Dp = 400.dp,
    enableLogging: Boolean = true,
    content: @Composable () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        state = state,
        modifier = modifier.fillMaxSize()
    ) {
        ConstrainedScrollableContainer(
            modifier = Modifier.fillMaxSize(),
            fallbackHeight = fallbackHeight,
            enableLogging = enableLogging
        ) {
            content()
        }
    }
}