package com.devpush.features.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import com.devpush.features.ui.components.ExpressivePerformance
import kotlinx.coroutines.delay

/**
 * Animation utilities for expressive Material 3 components.
 * Provides reusable animation functions for enhanced interactions.
 */
object ExpressiveAnimations {
    
    /**
     * Default spring specification for expressive animations.
     */
    val DefaultSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Quick spring specification for fast interactions.
     */
    val QuickSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * Slow spring specification for smooth, gentle animations.
     */
    val SlowSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * Creates a tween animation specification with expressive timing.
     */
    fun <T> expressiveTween(
        duration: Duration = ExpressiveTokens.Motion.Medium,
        easing: Easing = FastOutSlowInEasing
    ): TweenSpec<T> = tween(
        durationMillis = duration.inWholeMilliseconds.toInt(),
        easing = easing
    )
    
    /**
     * Creates an animated elevation value based on interaction state.
     */
    @Composable
    fun animatedElevation(
        interactionSource: InteractionSource,
        defaultElevation: Dp = ExpressiveTokens.Elevation.Level1,
        pressedElevation: Dp = ExpressiveTokens.Elevation.Level0,
        hoveredElevation: Dp = ExpressiveTokens.Elevation.Level2,
        focusedElevation: Dp = ExpressiveTokens.Elevation.Level2,
        animationSpec: AnimationSpec<Dp> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Dp> {
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        val targetElevation = when {
            isPressed -> pressedElevation
            isHovered -> hoveredElevation
            isFocused -> focusedElevation
            else -> defaultElevation
        }
        
        return animateDpAsState(
            targetValue = targetElevation,
            animationSpec = animationSpec,
            label = "elevation"
        )
    }
    
    /**
     * Creates an animated color value based on interaction state.
     */
    @Composable
    fun animatedStateLayerColor(
        interactionSource: InteractionSource,
        defaultColor: Color = Color.Transparent,
        pressedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        hoveredColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        focusedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        animationSpec: AnimationSpec<Color> = tween(
            durationMillis = ExpressiveTokens.Motion.Medium.inWholeMilliseconds.toInt(),
            easing = FastOutSlowInEasing
        )
    ): State<Color> {
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        val targetColor = when {
            isPressed -> pressedColor
            isHovered -> hoveredColor
            isFocused -> focusedColor
            else -> defaultColor
        }
        
        return animateColorAsState(
            targetValue = targetColor,
            animationSpec = animationSpec,
            label = "stateLayerColor"
        )
    }
    
    /**
     * Creates an animated scale value for press interactions.
     */
    @Composable
    fun animatedPressScale(
        interactionSource: InteractionSource,
        defaultScale: Float = 1f,
        pressedScale: Float = 0.96f,
        animationSpec: AnimationSpec<Float> = QuickSpring
    ): State<Float> {
        val isPressed by interactionSource.collectIsPressedAsState()
        
        return animateFloatAsState(
            targetValue = if (isPressed) pressedScale else defaultScale,
            animationSpec = animationSpec,
            label = "pressScale"
        )
    }
    
    /**
     * Creates an animated rotation value for FAB menu expansion.
     */
    @Composable
    fun animatedFABRotation(
        isExpanded: Boolean,
        animationSpec: AnimationSpec<Float> = DefaultSpring
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isExpanded) 45f else 0f,
            animationSpec = animationSpec,
            label = "fabRotation"
        )
    }
    
    /**
     * Creates an animated alpha value for fade in/out effects.
     */
    @Composable
    fun animatedAlpha(
        visible: Boolean,
        animationSpec: AnimationSpec<Float> = expressiveTween()
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = animationSpec,
            label = "alpha"
        )
    }
    
    /**
     * Creates an animated offset value for slide animations.
     */
    @Composable
    fun animatedSlideOffset(
        visible: Boolean,
        slideDistance: Dp = 16.dp,
        animationSpec: AnimationSpec<Dp> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Dp> {
        return animateDpAsState(
            targetValue = if (visible) 0.dp else slideDistance,
            animationSpec = animationSpec,
            label = "slideOffset"
        )
    }
    
    /**
     * Creates staggered animation delays for multiple items.
     */
    fun staggeredDelay(
        index: Int,
        baseDelay: Duration = 50.milliseconds,
        maxDelay: Duration = 200.milliseconds
    ): Duration {
        val delay = baseDelay * index
        return if (delay > maxDelay) maxDelay else delay
    }
    
    /**
     * Spring-based FAB menu expansion animation with natural motion.
     */
    @Composable
    fun animatedFABMenuExpansion(
        isExpanded: Boolean,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isExpanded) 1f else 0f,
            animationSpec = animationSpec,
            label = "fabMenuExpansion"
        )
    }
    
    /**
     * Staggered animation for individual FAB actions with performance optimization.
     */
    @Composable
    fun animatedFABActionVisibility(
        isVisible: Boolean,
        index: Int,
        staggerDelay: Duration = 50.milliseconds,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    ): State<Float> {
        var shouldAnimate by remember { mutableStateOf(false) }
        
        // Use optimized stagger delay based on performance preferences
        val optimizedDelay = ExpressivePerformance.optimizedStaggerDelay(staggerDelay, index)
        val canStartAnimation = ExpressivePerformance.MemoryManager.canStartNewAnimation()
        
        LaunchedEffect(isVisible) {
            if (isVisible && canStartAnimation) {
                val animationId = "fabAction_$index"
                ExpressivePerformance.MemoryManager.registerAnimation(animationId)
                
                if (optimizedDelay > Duration.ZERO) {
                    delay(optimizedDelay.times(index))
                }
                shouldAnimate = true
            } else {
                shouldAnimate = false
            }
        }
        
        return animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = accessibleAnimationSpec(animationSpec),
            label = "fabActionVisibility_$index",
            finishedListener = {
                ExpressivePerformance.MemoryManager.unregisterAnimation("fabAction_$index")
            }
        )
    }
    
    /**
     * Enhanced rotation animation for main FAB icon transformation with spring physics.
     */
    @Composable
    fun animatedFABIconRotation(
        isExpanded: Boolean,
        rotationAngle: Float = 45f,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isExpanded) rotationAngle else 0f,
            animationSpec = animationSpec,
            label = "fabIconRotation"
        )
    }
    
    /**
     * Smooth scale animation for FAB actions with spring physics.
     */
    @Composable
    fun animatedFABActionScale(
        isVisible: Boolean,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "fabActionScale"
        )
    }
    
    /**
     * Coordinated animation for FAB menu overlay with smooth fade.
     */
    @Composable
    fun animatedFABMenuOverlay(
        isVisible: Boolean,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = animationSpec,
            label = "fabMenuOverlay"
        )
    }
    
    /**
     * Enhanced focus indicator animation for text fields with spring physics.
     */
    @Composable
    fun animatedFocusIndicator(
        interactionSource: InteractionSource,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Float> {
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        return animateFloatAsState(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = animationSpec,
            label = "focusIndicator"
        )
    }
    
    /**
     * Smooth scale animation for focus feedback on text fields.
     */
    @Composable
    fun animatedFocusScale(
        interactionSource: InteractionSource,
        defaultScale: Float = 1f,
        focusedScale: Float = 1.02f,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    ): State<Float> {
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        return animateFloatAsState(
            targetValue = if (isFocused) focusedScale else defaultScale,
            animationSpec = animationSpec,
            label = "focusScale"
        )
    }
    
    /**
     * Enhanced state layer animation for card hover effects.
     */
    @Composable
    fun animatedCardStateLayer(
        interactionSource: InteractionSource,
        defaultAlpha: Float = 0f,
        hoveredAlpha: Float = 0.08f,
        pressedAlpha: Float = 0.12f,
        focusedAlpha: Float = 0.12f,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Float> {
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        val targetAlpha = when {
            isPressed -> pressedAlpha
            isHovered -> hoveredAlpha
            isFocused -> focusedAlpha
            else -> defaultAlpha
        }
        
        return animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = animationSpec,
            label = "cardStateLayer"
        )
    }
    
    /**
     * Enhanced button state layer animation with ripple coordination.
     */
    @Composable
    fun animatedButtonStateLayer(
        interactionSource: InteractionSource,
        baseColor: Color,
        defaultAlpha: Float = 0f,
        hoveredAlpha: Float = 0.08f,
        pressedAlpha: Float = 0.12f,
        focusedAlpha: Float = 0.12f,
        animationSpec: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Color> {
        val isPressed by interactionSource.collectIsPressedAsState()
        val isHovered by interactionSource.collectIsHoveredAsState()
        val isFocused by interactionSource.collectIsFocusedAsState()
        
        val targetAlpha = when {
            isPressed -> pressedAlpha
            isHovered -> hoveredAlpha
            isFocused -> focusedAlpha
            else -> defaultAlpha
        }
        
        val animatedAlpha by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = animationSpec,
            label = "buttonStateLayerAlpha"
        )
        
        return remember(baseColor, animatedAlpha) {
            derivedStateOf { baseColor.copy(alpha = animatedAlpha) }
        }
    }
    
    /**
     * Creates an infinite rotation animation for loading indicators.
     */
    @Composable
    fun infiniteRotation(
        duration: Duration = 1000.milliseconds
    ): State<Float> {
        val infiniteTransition = rememberInfiniteTransition(label = "infiniteRotation")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = LinearEasing
                )
            ),
            label = "rotation"
        )
    }
    
    /**
     * Creates a pulsing animation for attention-grabbing effects.
     */
    @Composable
    fun pulseAnimation(
        minScale: Float = 0.95f,
        maxScale: Float = 1.05f,
        duration: Duration = 1000.milliseconds
    ): State<Float> {
        val infiniteTransition = rememberInfiniteTransition(label = "pulseAnimation")
        return infiniteTransition.animateFloat(
            initialValue = minScale,
            targetValue = maxScale,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = duration.inWholeMilliseconds.toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
    }
    
    /**
     * Creates smooth transition functions for color changes.
     */
    @Composable
    fun smoothColorTransition(
        targetColor: Color,
        animationSpec: AnimationSpec<Color> = expressiveTween(
            duration = ExpressiveTokens.Motion.Medium
        )
    ): State<Color> {
        return animateColorAsState(
            targetValue = targetColor,
            animationSpec = animationSpec,
            label = "colorTransition"
        )
    }
    
    /**
     * Creates smooth transition functions for elevation changes.
     */
    @Composable
    fun smoothElevationTransition(
        targetElevation: Dp,
        animationSpec: AnimationSpec<Dp> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): State<Dp> {
        return animateDpAsState(
            targetValue = targetElevation,
            animationSpec = animationSpec,
            label = "elevationTransition"
        )
    }
    
    /**
     * Utility function to check if animations should be disabled based on system preferences.
     */
    @Composable
    fun shouldDisableAnimations(): Boolean {
        return ExpressivePerformance.shouldUseReducedMotion()
    }
    
    /**
     * Creates a conditional animation spec that respects accessibility and performance preferences.
     */
    @Composable
    fun <T> accessibleAnimationSpec(
        animationSpec: AnimationSpec<T>,
        snapSpec: AnimationSpec<T> = snap()
    ): AnimationSpec<T> {
        return ExpressivePerformance.optimizedAnimationSpec(
            defaultSpec = animationSpec,
            reducedMotionSpec = snapSpec
        )
    }
    
    /**
     * Creates a performance-aware spring animation spec.
     */
    @Composable
    fun <T> performanceAwareSpring(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): AnimationSpec<T> {
        return ExpressivePerformance.optimizedSpringSpec(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
    }
}

/**
 * Extension functions for common animation patterns.
 */

/**
 * Animates a float value with expressive timing.
 */
@Composable
fun animateExpressiveFloat(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = ExpressiveAnimations.DefaultSpring,
    label: String = "expressiveFloat"
): State<Float> {
    return animateFloatAsState(
        targetValue = targetValue,
        animationSpec = ExpressiveAnimations.accessibleAnimationSpec(animationSpec),
        label = label
    )
}

/**
 * Animates a Dp value with expressive timing.
 */
@Composable
fun animateExpressiveDp(
    targetValue: Dp,
    animationSpec: AnimationSpec<Dp> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    label: String = "expressiveDp"
): State<Dp> {
    return animateDpAsState(
        targetValue = targetValue,
        animationSpec = ExpressiveAnimations.accessibleAnimationSpec(animationSpec),
        label = label
    )
}

/**
 * Animates a Color value with expressive timing.
 */
@Composable
fun animateExpressiveColor(
    targetValue: Color,
    animationSpec: AnimationSpec<Color> = tween(
        durationMillis = ExpressiveTokens.Motion.Medium.inWholeMilliseconds.toInt(),
        easing = FastOutSlowInEasing
    ),
    label: String = "expressiveColor"
): State<Color> {
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = ExpressiveAnimations.accessibleAnimationSpec(animationSpec),
        label = label
    )
}