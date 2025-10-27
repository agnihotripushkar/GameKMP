package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.FloatingActionButtonElevation

import com.devpush.features.ui.theme.ExpressiveAnimations
import com.devpush.features.ui.theme.ExpressiveTokens
import com.devpush.features.ui.theme.animateExpressiveFloat

/**
 * Main FAB component with expressive styling, rotation animation, accessibility, and responsive design support.
 * Uses Material 3 FloatingActionButton with enhanced visual effects and state management.
 */
@Composable
fun ExpressiveMainFAB(
    isExpanded: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expandedIcon: ImageVector = Icons.Default.Close,
    collapsedIcon: ImageVector = Icons.Default.Add,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String = if (isExpanded) "Close menu" else "Open menu",
    focusRequester: FocusRequester? = null,
    semantics: (SemanticsPropertyReceiver.() -> Unit)? = null,
    size: Dp = ExpressiveResponsive.adaptiveFABSize()
) {
    // Performance-aware rotation animation
    val rotation by if (ExpressivePerformance.shouldUseReducedMotion()) {
        ExpressivePerformance.ReducedMotionAlternatives.reducedMotionRotation(
            targetRotation = if (isExpanded) 45f else 0f,
            label = "mainFABRotation"
        )
    } else {
        ExpressiveAnimations.animatedFABIconRotation(
            isExpanded = isExpanded,
            rotationAngle = 45f,
            animationSpec = ExpressiveAnimations.performanceAwareSpring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    // Performance-aware elevation animation
    val animatedElevation by if (ExpressivePerformance.shouldEnableComplexAnimations()) {
        ExpressiveAnimations.animatedElevation(
            interactionSource = interactionSource,
            defaultElevation = ExpressiveResponsive.adaptiveElevation(),
            pressedElevation = ExpressiveResponsive.adaptiveElevation() / 2,
            hoveredElevation = ExpressiveResponsive.adaptiveElevation() * 1.5f,
            focusedElevation = ExpressiveResponsive.adaptiveElevation() * 1.5f,
            animationSpec = ExpressiveAnimations.performanceAwareSpring()
        )
    } else {
        // Static elevation for reduced performance
        val staticElevation = ExpressiveResponsive.adaptiveElevation()
        remember { mutableStateOf(staticElevation) }
    }
    

    
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
            .size(
                // Responsive sizing with accessibility compliance
                ExpressiveResponsive.adaptiveTouchTargetSize(size)
            )
            .rotate(rotation)
            .let { mod ->
                focusRequester?.let { requester ->
                    mod.then(
                        ExpressiveAccessibility.focusableWithSemantics(
                            focusRequester = requester,
                            semantics = semantics
                        )
                    )
                } ?: mod.semantics {
                    this.contentDescription = contentDescription
                    role = Role.Button
                    
                    // State description for screen readers
                    stateDescription = if (isExpanded) "Expanded" else "Collapsed"
                    
                    // Expandable semantics
                    if (!isExpanded) {
                        expand {
                            onClick()
                            true
                        }
                    } else {
                        collapse {
                            onClick()
                            true
                        }
                    }
                    
                    // Apply custom semantics if provided
                    semantics?.invoke(this)
                }
            },
        shape = RoundedCornerShape(ExpressiveResponsive.adaptiveCornerRadius()),
        containerColor = containerColor,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = animatedElevation,
            pressedElevation = ExpressiveResponsive.adaptiveElevation() / 2,
            focusedElevation = ExpressiveResponsive.adaptiveElevation() * 1.5f,
            hoveredElevation = ExpressiveResponsive.adaptiveElevation() * 1.5f
        ),
        interactionSource = interactionSource
    ) {
        // Performance-aware icon transition
        val iconTransition by if (ExpressivePerformance.shouldEnableComplexAnimations()) {
            animatePerformanceAwareFloat(
                targetValue = if (isExpanded) 1f else 0f,
                animationSpec = ExpressiveAnimations.performanceAwareSpring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessHigh
                ),
                label = "mainFABIconTransition"
            )
        } else {
            // Instant transition for reduced performance
            remember(isExpanded) { mutableStateOf(if (isExpanded) 1f else 0f) }
        }
        
        // Icon morphing based on transition progress
        val currentIcon = if (iconTransition < 0.5f) collapsedIcon else expandedIcon
        
        Icon(
            imageVector = currentIcon,
            contentDescription = null,
            modifier = Modifier
                .rotate(-rotation) // Counter-rotate to keep icon upright
                .size(
                    // Responsive icon size
                    when (ExpressiveResponsive.getScreenSize()) {
                        ExpressiveResponsive.ScreenSize.Compact -> 24.dp
                        ExpressiveResponsive.ScreenSize.Medium -> 28.dp
                        ExpressiveResponsive.ScreenSize.Expanded -> 32.dp
                    }
                )
        )
    }
}

/**
 * Expressive FAB colors with enhanced theming support.
 */
object ExpressiveMainFABDefaults {
    
    /**
     * Creates expressive colors for the main FAB with enhanced visual appeal.
     */

    
    /**
     * Creates expressive elevation for the main FAB with enhanced depth.
     */
    @Composable
    fun elevation(
        defaultElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3,
        pressedElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level2,
        focusedElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level4,
        hoveredElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level4
    ): FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation
    )
}

/**
 * Preview composable for the ExpressiveMainFAB component.
 */
@Composable
internal fun ExpressiveMainFABPreview() {
    var isExpanded by remember { mutableStateOf(false) }
    
    MaterialTheme {
        ExpressiveMainFAB(
            isExpanded = isExpanded,
            onClick = { isExpanded = !isExpanded }
        )
    }
}