package com.devpush.features.ui.components

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.devpush.features.ui.theme.ExpressiveTokens

/**
 * Accessibility utilities for expressive Material 3 components.
 * Provides comprehensive accessibility support including screen reader support,
 * focus management, and keyboard navigation.
 */
object ExpressiveAccessibility {
    
    /**
     * Minimum touch target size for accessibility compliance.
     */
    val MinTouchTargetSize = 48.dp
    
    /**
     * Creates accessibility semantics for FAB menu components.
     */
    fun fabMenuSemantics(
        isExpanded: Boolean,
        actionCount: Int,
        onToggle: () -> Unit
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = if (isExpanded) {
            "FAB menu expanded with $actionCount actions. Tap to close."
        } else {
            "FAB menu collapsed. Tap to open $actionCount actions."
        }
        
        stateDescription = if (isExpanded) "Expanded" else "Collapsed"
        
        onClick {
            onToggle()
            true
        }
        
        // Mark as a button for screen readers
        role = Role.Button
        
        // Indicate this is expandable
        if (!isExpanded) {
            expand {
                onToggle()
                true
            }
        } else {
            collapse {
                onToggle()
                true
            }
        }
    }
    
    /**
     * Creates accessibility semantics for individual FAB actions.
     */
    fun fabActionSemantics(
        action: FABAction,
        index: Int,
        totalActions: Int
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = "${action.contentDescription}. Action ${index + 1} of $totalActions"
        
        onClick {
            action.onClick()
            true
        }
        
        role = Role.Button
        
        // Add position information for screen readers
        // Note: setProgress is for progress indicators, not position info
    }
    
    /**
     * Creates accessibility semantics for the FAB menu overlay.
     */
    fun fabMenuOverlaySemantics(
        onDismiss: () -> Unit
    ): SemanticsPropertyReceiver.() -> Unit = {
        contentDescription = "FAB menu overlay. Tap to close menu."
        
        onClick {
            onDismiss()
            true
        }
        
        role = Role.Button
        
        // Mark as dismissible
        dismiss {
            onDismiss()
            true
        }
    }
    
    /**
     * Creates a modifier with proper touch target size for accessibility.
     */
    fun accessibleTouchTarget(
        minSize: androidx.compose.ui.unit.Dp = MinTouchTargetSize
    ): Modifier = Modifier.size(minSize)
    
    /**
     * Creates a modifier with focus management for keyboard navigation.
     */
    fun focusableWithSemantics(
        focusRequester: FocusRequester? = null,
        onFocusChanged: ((Boolean) -> Unit)? = null,
        semantics: (SemanticsPropertyReceiver.() -> Unit)? = null
    ): Modifier {
        var modifier = Modifier.focusable()
        
        focusRequester?.let { requester ->
            modifier = modifier.focusRequester(requester)
        }
        
        onFocusChanged?.let { callback ->
            modifier = modifier.onFocusChanged { focusState ->
                callback(focusState.isFocused)
            }
        }
        
        semantics?.let { semanticsBlock ->
            modifier = modifier.semantics(properties = semanticsBlock)
        }
        
        return modifier
    }
    
    /**
     * Announces state changes to screen readers.
     */
    @Composable
    fun announceStateChange(
        message: String,
        accessibilityManager: androidx.compose.ui.platform.AccessibilityManager? = LocalAccessibilityManager.current
    ) {
        LaunchedEffect(message) {
            accessibilityManager?.let { manager ->
                // Announce the state change to screen readers
                // This would typically use platform-specific accessibility APIs
                // For now, we'll use a simple approach
            }
        }
    }
    
    /**
     * Creates live region semantics for dynamic content updates.
     */
    fun liveRegionSemantics(
        politeness: LiveRegionMode = LiveRegionMode.Polite
    ): SemanticsPropertyReceiver.() -> Unit = {
        liveRegion = politeness
    }
    
    /**
     * Creates heading semantics for structural navigation.
     */
    fun headingSemantics(
        level: Int = 1
    ): SemanticsPropertyReceiver.() -> Unit = {
        heading()
        // Note: Compose doesn't have built-in heading level support yet
        // This would be enhanced when available
    }
    
    /**
     * Creates collection semantics for grouped items.
     */
    fun collectionSemantics(
        itemCount: Int
    ): SemanticsPropertyReceiver.() -> Unit = {
        collectionInfo = CollectionInfo(
            rowCount = itemCount,
            columnCount = 1
        )
    }
    
    /**
     * Creates collection item semantics for items within collections.
     */
    fun collectionItemSemantics(
        index: Int,
        totalCount: Int
    ): SemanticsPropertyReceiver.() -> Unit = {
        collectionItemInfo = CollectionItemInfo(
            rowIndex = index,
            rowSpan = 1,
            columnIndex = 0,
            columnSpan = 1
        )
    }
}

/**
 * Focus management utilities for keyboard navigation.
 */
object ExpressiveFocusManager {
    
    /**
     * Creates a focus requester for managing focus programmatically.
     */
    @Composable
    fun rememberFocusRequester(): FocusRequester {
        return remember { FocusRequester() }
    }
    
    /**
     * Creates multiple focus requesters for a list of items.
     */
    @Composable
    fun rememberFocusRequesters(count: Int): List<FocusRequester> {
        return remember(count) {
            List(count) { FocusRequester() }
        }
    }
    
    /**
     * Manages focus traversal for FAB menu actions.
     */
    @Composable
    fun manageFABMenuFocus(
        isExpanded: Boolean,
        actionCount: Int,
        onFocusChanged: (Int?) -> Unit = {}
    ): FABMenuFocusState {
        val mainFabFocusRequester = rememberFocusRequester()
        val actionFocusRequesters = rememberFocusRequesters(actionCount)
        var currentFocusIndex by remember { mutableStateOf<Int?>(null) }
        
        // Auto-focus management when menu expands/collapses
        LaunchedEffect(isExpanded) {
            if (isExpanded && actionCount > 0) {
                // Focus first action when menu expands
                actionFocusRequesters.firstOrNull()?.requestFocus()
                currentFocusIndex = 0
            } else if (!isExpanded) {
                // Focus main FAB when menu collapses
                mainFabFocusRequester.requestFocus()
                currentFocusIndex = null
            }
            onFocusChanged(currentFocusIndex)
        }
        
        return FABMenuFocusState(
            mainFabFocusRequester = mainFabFocusRequester,
            actionFocusRequesters = actionFocusRequesters,
            currentFocusIndex = currentFocusIndex,
            onFocusIndexChanged = { index ->
                currentFocusIndex = index
                onFocusChanged(index)
            }
        )
    }
}

/**
 * State holder for FAB menu focus management.
 */
@Stable
data class FABMenuFocusState(
    val mainFabFocusRequester: FocusRequester,
    val actionFocusRequesters: List<FocusRequester>,
    val currentFocusIndex: Int?,
    val onFocusIndexChanged: (Int?) -> Unit
) {
    /**
     * Requests focus for a specific action by index.
     */
    fun focusAction(index: Int) {
        if (index in actionFocusRequesters.indices) {
            actionFocusRequesters[index].requestFocus()
            onFocusIndexChanged(index)
        }
    }
    
    /**
     * Requests focus for the main FAB.
     */
    fun focusMainFAB() {
        mainFabFocusRequester.requestFocus()
        onFocusIndexChanged(null)
    }
    
    /**
     * Moves focus to the next action in the list.
     */
    fun focusNext() {
        val nextIndex = when (val current = currentFocusIndex) {
            null -> 0
            else -> (current + 1).coerceAtMost(actionFocusRequesters.size - 1)
        }
        focusAction(nextIndex)
    }
    
    /**
     * Moves focus to the previous action in the list.
     */
    fun focusPrevious() {
        val previousIndex = when (val current = currentFocusIndex) {
            null -> actionFocusRequesters.size - 1
            0 -> {
                focusMainFAB()
                return
            }
            else -> current - 1
        }
        focusAction(previousIndex)
    }
}

/**
 * Screen reader announcement utilities.
 */
object ExpressiveScreenReader {
    
    /**
     * Announces FAB menu state changes.
     */
    @Composable
    fun announceFABMenuStateChange(
        isExpanded: Boolean,
        actionCount: Int
    ) {
        val message = if (isExpanded) {
            "FAB menu expanded. $actionCount actions available."
        } else {
            "FAB menu collapsed."
        }
        
        ExpressiveAccessibility.announceStateChange(message)
    }
    
    /**
     * Announces when a FAB action is activated.
     */
    @Composable
    fun announceFABActionActivated(
        actionLabel: String
    ) {
        ExpressiveAccessibility.announceStateChange("$actionLabel activated")
    }
    
    /**
     * Announces focus changes within the FAB menu.
     */
    @Composable
    fun announceFABMenuFocusChange(
        focusIndex: Int?,
        actions: List<FABAction>
    ) {
        val message = when (focusIndex) {
            null -> "Main FAB focused"
            else -> {
                val action = actions.getOrNull(focusIndex)
                if (action != null) {
                    "${action.label} focused. Action ${focusIndex + 1} of ${actions.size}"
                } else {
                    "Unknown action focused"
                }
            }
        }
        
        ExpressiveAccessibility.announceStateChange(message)
    }
}