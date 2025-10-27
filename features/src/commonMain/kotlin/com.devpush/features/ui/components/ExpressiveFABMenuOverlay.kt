package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.devpush.features.ui.theme.ExpressiveAnimations
import com.devpush.features.ui.theme.ExpressiveTokens

/**
 * Semi-transparent overlay for the FAB menu with tap-to-dismiss functionality and accessibility support.
 * Uses Material 3 Surface with expressive styling and smooth fade animations.
 */
@Composable
fun ExpressiveFABMenuOverlay(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    overlayColor: Color = MaterialTheme.colorScheme.scrim,
    overlayAlpha: Float = ExpressiveTokens.FABMenu.OverlayAlpha,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    // Enhanced overlay animation with spring-based fade
    val animatedAlpha by ExpressiveAnimations.animatedFABMenuOverlay(
        isVisible = isVisible,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    if (animatedAlpha > 0f) {
        Surface(
            modifier = modifier
                .fillMaxSize()
                .alpha(animatedAlpha)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null, // No ripple effect for overlay
                    onClick = onDismiss
                )
                .semantics {
                    // Enhanced accessibility semantics
                    ExpressiveAccessibility.fabMenuOverlaySemantics(onDismiss)(this)
                },
            color = overlayColor.copy(alpha = overlayAlpha * animatedAlpha),
            content = {}
        )
    }
}

/**
 * Complete FAB menu overlay with backdrop and content positioning.
 * Provides a full-screen overlay with proper touch handling and accessibility.
 */
@Composable
fun ExpressiveFABMenuBackdrop(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    overlayColor: Color = ExpressiveFABMenuOverlayDefaults.overlayColor(),
    overlayAlpha: Float = ExpressiveTokens.FABMenu.OverlayAlpha,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize()) {
        // Semi-transparent overlay
        ExpressiveFABMenuOverlay(
            isVisible = isVisible,
            onDismiss = onDismiss,
            overlayColor = overlayColor,
            overlayAlpha = overlayAlpha
        )
        
        // Content on top of overlay
        content()
    }
}

/**
 * Scrim overlay that appears behind the FAB menu when expanded with accessibility support.
 * Provides visual separation and tap-to-dismiss functionality.
 */
@Composable
fun ExpressiveFABMenuScrim(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    scrimColor: Color = ExpressiveFABMenuOverlayDefaults.scrimColor(),
    scrimAlpha: Float = ExpressiveTokens.FABMenu.OverlayAlpha
) {
    val animatedAlpha by ExpressiveAnimations.animatedFABMenuOverlay(
        isVisible = isVisible,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    if (animatedAlpha > 0f) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(scrimColor.copy(alpha = scrimAlpha * animatedAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .semantics {
                    // Enhanced accessibility semantics
                    ExpressiveAccessibility.fabMenuOverlaySemantics(onDismiss)(this)
                }
        )
    }
}

/**
 * Default values for FAB menu overlay components.
 */
object ExpressiveFABMenuOverlayDefaults {
    
    /**
     * Default overlay color using Material 3 scrim color.
     */
    @Composable
    fun overlayColor(): Color = MaterialTheme.colorScheme.scrim
    
    /**
     * Default scrim color for backdrop overlay.
     */
    @Composable
    fun scrimColor(): Color = MaterialTheme.colorScheme.scrim
    
    /**
     * Default overlay alpha value.
     */
    const val DefaultOverlayAlpha = 0.6f
    
    /**
     * Reduced overlay alpha for subtle backdrop.
     */
    const val SubtleOverlayAlpha = 0.3f
    
    /**
     * Enhanced overlay alpha for strong backdrop.
     */
    const val StrongOverlayAlpha = 0.8f
    
    /**
     * Creates a surface color for overlay with proper theming.
     */
    @Composable
    fun surfaceColor(
        baseColor: Color = MaterialTheme.colorScheme.surface,
        alpha: Float = DefaultOverlayAlpha
    ): Color = baseColor.copy(alpha = alpha)
    
    /**
     * Creates overlay colors for different themes.
     */
    @Composable
    fun overlayColors(
        alpha: Float = DefaultOverlayAlpha
    ): Color {
        // Use scrim color from theme which adapts to light/dark automatically
        return MaterialTheme.colorScheme.scrim.copy(alpha = alpha)
    }
}

/**
 * Utility functions for overlay management.
 */
object FABMenuOverlayUtils {
    
    /**
     * Calculates the appropriate overlay alpha based on content behind.
     */
    fun calculateOverlayAlpha(
        baseAlpha: Float = ExpressiveTokens.FABMenu.OverlayAlpha,
        contentDensity: Float = 1f,
        isHighContrast: Boolean = false
    ): Float {
        val adjustedAlpha = baseAlpha * contentDensity
        return if (isHighContrast) {
            (adjustedAlpha * 1.2f).coerceAtMost(0.9f)
        } else {
            adjustedAlpha.coerceIn(0.2f, 0.8f)
        }
    }
    
    /**
     * Determines if overlay should be visible based on menu state.
     */
    fun shouldShowOverlay(
        isExpanded: Boolean,
        isAnimating: Boolean = false,
        expandProgress: Float = 0f
    ): Boolean {
        return isExpanded || (isAnimating && expandProgress > 0f)
    }
    
    /**
     * Creates overlay modifier with proper touch handling.
     */
    fun overlayModifier(
        onDismiss: () -> Unit,
        isEnabled: Boolean = true
    ): Modifier {
        return if (isEnabled) {
            Modifier.clickable(
                interactionSource = MutableInteractionSource(),
                indication = null,
                onClick = onDismiss
            )
        } else {
            Modifier
        }
    }
}