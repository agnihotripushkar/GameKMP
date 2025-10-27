package com.devpush.features.ui.components

import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * State holder for FAB menu expansion and animation states.
 * Manages the current state of the FAB menu including expansion status and animation progress.
 */
@Stable
data class FABMenuState(
    val isExpanded: Boolean = false,
    val isAnimating: Boolean = false,
    val expandProgress: Float = 0f
) {
    /**
     * Creates a new state with updated expansion status.
     */
    fun withExpanded(expanded: Boolean): FABMenuState {
        return copy(isExpanded = expanded)
    }
    
    /**
     * Creates a new state with updated animation status.
     */
    fun withAnimating(animating: Boolean): FABMenuState {
        return copy(isAnimating = animating)
    }
    
    /**
     * Creates a new state with updated expand progress.
     */
    fun withExpandProgress(progress: Float): FABMenuState {
        return copy(expandProgress = progress.coerceIn(0f, 1f))
    }
}

/**
 * Configuration for individual FAB action items.
 * Represents a single action in the expandable FAB menu.
 */
@Immutable
data class FABAction(
    val icon: ImageVector,
    val label: String,
    val contentDescription: String,
    val onClick: () -> Unit,
    val colors: FABActionColors? = null
) {
    companion object {
        /**
         * Creates a FAB action with automatic content description based on label.
         */
        fun create(
            icon: ImageVector,
            label: String,
            onClick: () -> Unit,
            colors: FABActionColors? = null,
            contentDescription: String = label
        ): FABAction {
            return FABAction(
                icon = icon,
                label = label,
                contentDescription = contentDescription,
                onClick = onClick,
                colors = colors
            )
        }
    }
}

/**
 * Color configuration for FAB actions.
 * Provides colors for different states of FAB action buttons.
 */
@Immutable
data class FABActionColors(
    val containerColor: Color,
    val contentColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color
)

/**
 * Default values and factory functions for FAB actions.
 */
object FABActionDefaults {
    /**
     * Creates default colors for FAB actions using Material 3 color scheme.
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
        disabledContainerColor: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.38f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.38f)
    ): FABActionColors = FABActionColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
}

/**
 * State management functions for FAB menu expand/collapse logic.
 */
object FABMenuStateManager {
    
    /**
     * Toggles the expansion state of the FAB menu.
     */
    fun toggleExpansion(currentState: FABMenuState): FABMenuState {
        return currentState.withExpanded(!currentState.isExpanded)
    }
    
    /**
     * Expands the FAB menu if it's currently collapsed.
     */
    fun expand(currentState: FABMenuState): FABMenuState {
        return if (!currentState.isExpanded) {
            currentState.withExpanded(true)
        } else {
            currentState
        }
    }
    
    /**
     * Collapses the FAB menu if it's currently expanded.
     */
    fun collapse(currentState: FABMenuState): FABMenuState {
        return if (currentState.isExpanded) {
            currentState.withExpanded(false)
        } else {
            currentState
        }
    }
    
    /**
     * Updates the animation state during transitions.
     */
    fun updateAnimationState(
        currentState: FABMenuState,
        isAnimating: Boolean,
        progress: Float = currentState.expandProgress
    ): FABMenuState {
        return currentState
            .withAnimating(isAnimating)
            .withExpandProgress(progress)
    }
    
    /**
     * Handles the start of an expansion animation.
     */
    fun startExpansionAnimation(currentState: FABMenuState): FABMenuState {
        return currentState
            .withExpanded(true)
            .withAnimating(true)
            .withExpandProgress(0f)
    }
    
    /**
     * Handles the start of a collapse animation.
     */
    fun startCollapseAnimation(currentState: FABMenuState): FABMenuState {
        return currentState
            .withExpanded(false)
            .withAnimating(true)
            .withExpandProgress(1f)
    }
    
    /**
     * Handles the completion of any animation.
     */
    fun completeAnimation(currentState: FABMenuState): FABMenuState {
        return currentState
            .withAnimating(false)
            .withExpandProgress(if (currentState.isExpanded) 1f else 0f)
    }
    
    /**
     * Resets the FAB menu to its initial collapsed state.
     */
    fun reset(): FABMenuState {
        return FABMenuState()
    }
}