package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devpush.features.ui.theme.ExpressiveAnimations
import com.devpush.features.ui.theme.ExpressiveTokens
import com.devpush.features.ui.theme.animateExpressiveFloat
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Individual FAB action item with expressive styling, animations, accessibility, and responsive design support.
 * Uses Material 3 SmallFloatingActionButton with text labels and staggered animations.
 */
@Composable
fun ExpressiveFABAction(
    action: FABAction,
    isVisible: Boolean,
    animationDelay: Long = 0L,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    focusRequester: FocusRequester? = null,
    index: Int = 0,
    totalActions: Int = 1,
    responsiveConfig: ResponsiveFABConfig = ResponsiveFABConfig.adaptive()
) {
    // Performance-aware staggered animation
    val animationProgress by ExpressiveAnimations.animatedFABActionVisibility(
        isVisible = isVisible,
        index = (animationDelay / 50L).toInt(),
        staggerDelay = animationDelay.milliseconds,
        animationSpec = ExpressiveAnimations.performanceAwareSpring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    // Performance-aware scale animation
    val animatedScale by if (ExpressivePerformance.shouldUseReducedMotion()) {
        // Use reduced motion alternative
        ExpressivePerformance.ReducedMotionAlternatives.reducedMotionScale(
            targetScale = if (animationProgress > 0f) 1f else 0f,
            label = "fabActionScale_$index"
        )
    } else {
        ExpressiveAnimations.animatedFABActionScale(
            isVisible = animationProgress > 0f,
            animationSpec = ExpressiveAnimations.performanceAwareSpring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
    }
    
    // Performance-aware alpha transition
    val animatedAlpha by if (ExpressivePerformance.shouldUseReducedMotion()) {
        ExpressivePerformance.ReducedMotionAlternatives.reducedMotionFade(
            visible = animationProgress > 0f,
            label = "fabActionAlpha_$index"
        )
    } else {
        remember(animationProgress) { mutableStateOf(animationProgress) }
    }
    
    // Performance-aware slide animation
    val slideOffset by animatePerformanceAwareFloat(
        targetValue = if (animationProgress > 0f) 0f else 16f,
        animationSpec = ExpressiveAnimations.performanceAwareSpring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabActionSlide_$index"
    )
    
    // Performance-aware elevation animation
    val animatedElevation by if (ExpressivePerformance.shouldEnableComplexAnimations()) {
        ExpressiveAnimations.animatedElevation(
            interactionSource = interactionSource,
            defaultElevation = responsiveConfig.elevation,
            pressedElevation = responsiveConfig.elevation / 2,
            hoveredElevation = responsiveConfig.elevation * 1.5f,
            focusedElevation = responsiveConfig.elevation * 1.5f,
            animationSpec = ExpressiveAnimations.performanceAwareSpring()
        )
    } else {
        // Static elevation for reduced performance
        remember { mutableStateOf(responsiveConfig.elevation) }
    }
    
    Row(
        modifier = modifier
            .alpha(animatedAlpha)
            .scale(animatedScale)
            .offset(y = slideOffset.dp)
            .let { mod ->
                focusRequester?.let { requester ->
                    mod.then(
                        ExpressiveAccessibility.focusableWithSemantics(
                            focusRequester = requester,
                            semantics = ExpressiveAccessibility.fabActionSemantics(
                                action = action,
                                index = index,
                                totalActions = totalActions
                            )
                        )
                    )
                } ?: mod.semantics {
                    ExpressiveAccessibility.fabActionSemantics(
                        action = action,
                        index = index,
                        totalActions = totalActions
                    )(this)
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        // Text label with responsive styling (only show if configured)
        if (responsiveConfig.showLabels) {
            Surface(
                modifier = Modifier.padding(end = responsiveConfig.spacing),
                shape = RoundedCornerShape(responsiveConfig.cornerRadius),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = responsiveConfig.elevation,
                tonalElevation = responsiveConfig.elevation
            ) {
                Text(
                    text = action.label,
                    modifier = Modifier.padding(
                        horizontal = responsiveConfig.spacing,
                        vertical = responsiveConfig.spacing / 2
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Small FAB with responsive sizing, expressive styling and accessibility support
        SmallFloatingActionButton(
            onClick = action.onClick,
            modifier = Modifier
                .size(
                    // Responsive sizing with accessibility compliance
                    ExpressiveResponsive.adaptiveTouchTargetSize(responsiveConfig.actionFABSize)
                )
                .semantics {
                    // Enhanced semantics for screen readers
                    contentDescription = "${action.contentDescription}. Button ${index + 1} of $totalActions"
                    role = Role.Button
                    
                    // Add collection item information
                    ExpressiveAccessibility.collectionItemSemantics(index, totalActions)(this)
                },
            shape = RoundedCornerShape(responsiveConfig.cornerRadius),
            containerColor = action.colors?.containerColor ?: MaterialTheme.colorScheme.primaryContainer,
            contentColor = action.colors?.contentColor ?: MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = animatedElevation,
                pressedElevation = responsiveConfig.elevation / 2,
                focusedElevation = responsiveConfig.elevation * 1.5f,
                hoveredElevation = responsiveConfig.elevation * 1.5f
            ),
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null, // Content description is handled by the button
                modifier = Modifier.size(
                    // Responsive icon size
                    when (ExpressiveResponsive.getScreenSize()) {
                        ExpressiveResponsive.ScreenSize.Compact -> 18.dp
                        ExpressiveResponsive.ScreenSize.Medium -> 20.dp
                        ExpressiveResponsive.ScreenSize.Expanded -> 22.dp
                    }
                )
            )
        }
    }
}

/**
 * Container for multiple FAB actions with staggered animations, accessibility, and responsive design support.
 * Manages the layout, animation timing, and focus management for all FAB action items.
 */
@Composable
fun ExpressiveFABActionsList(
    actions: List<FABAction>,
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    staggerDelay: Long = 50L,
    focusState: FABMenuFocusState? = null,
    responsiveConfig: ResponsiveFABConfig = ResponsiveFABConfig.adaptive()
) {
    Column(
        modifier = modifier.semantics {
            // Collection semantics for screen readers
            ExpressiveAccessibility.collectionSemantics(actions.size)(this)
            
            // Live region for dynamic content
            liveRegion = LiveRegionMode.Polite
        },
        verticalArrangement = Arrangement.spacedBy(responsiveConfig.spacing),
        horizontalAlignment = Alignment.End
    ) {
        actions.forEachIndexed { index, action ->
            ExpressiveFABAction(
                action = action,
                isVisible = isVisible,
                animationDelay = if (isVisible) {
                    staggerDelay * index
                } else 0L,
                focusRequester = focusState?.actionFocusRequesters?.getOrNull(index),
                index = index,
                totalActions = actions.size,
                responsiveConfig = responsiveConfig
            )
        }
    }
}

/**
 * Default values for FAB actions with expressive styling.
 */
object ExpressiveFABActionDefaults {
    
    /**
     * Creates expressive colors for FAB actions.
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): FABActionColors = FABActionColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive elevation for FAB actions.
     */
    @Composable
    fun elevation(
        defaultElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level2,
        pressedElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level1,
        focusedElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3,
        hoveredElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3
    ): FloatingActionButtonElevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation
    )
    
    /**
     * Standard stagger delay for action animations.
     */
    const val DefaultStaggerDelay = 50L
    
    /**
     * Maximum number of actions recommended for optimal UX.
     */
    const val MaxRecommendedActions = 6
}

/**
 * Utility functions for creating common FAB actions.
 */
object FABActionFactory {
    
    /**
     * Creates a FAB action with default expressive styling.
     */
    @Composable
    fun createAction(
        icon: androidx.compose.ui.graphics.vector.ImageVector,
        label: String,
        onClick: () -> Unit,
        contentDescription: String = label,
        colors: FABActionColors = ExpressiveFABActionDefaults.colors()
    ): FABAction {
        return FABAction(
            icon = icon,
            label = label,
            contentDescription = contentDescription,
            onClick = onClick,
            colors = colors
        )
    }
    
    /**
     * Creates multiple FAB actions with consistent styling.
     */
    @Composable
    fun createActions(
        vararg actionConfigs: Triple<androidx.compose.ui.graphics.vector.ImageVector, String, () -> Unit>,
        colors: FABActionColors = ExpressiveFABActionDefaults.colors()
    ): List<FABAction> {
        return actionConfigs.map { (icon, label, onClick) ->
            createAction(
                icon = icon,
                label = label,
                onClick = onClick,
                colors = colors
            )
        }
    }
}