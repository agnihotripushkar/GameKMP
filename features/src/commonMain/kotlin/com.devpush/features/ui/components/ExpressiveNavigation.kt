package com.devpush.features.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.IntOffset
import com.devpush.features.ui.theme.ExpressiveMotion
import com.devpush.features.ui.theme.SlideDirection

/**
 * Expressive Navigation Transitions with enhanced motion choreography.
 * Provides smooth page transitions with proper Material 3 motion patterns.
 */
object ExpressiveNavigation {
    
    /**
     * Creates a slide transition for navigation between screens.
     */
    fun slideTransition(
        direction: SlideDirection = SlideDirection.Left,
        animationSpec: FiniteAnimationSpec<IntOffset> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): ContentTransform {
        return when (direction) {
            SlideDirection.Left -> slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = animationSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = animationSpec
            )
            SlideDirection.Right -> slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = animationSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = animationSpec
            )
            SlideDirection.Up -> slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = animationSpec
            ) togetherWith slideOutVertically(
                targetOffsetY = { fullHeight -> -fullHeight },
                animationSpec = animationSpec
            )
            SlideDirection.Down -> slideInVertically(
                initialOffsetY = { fullHeight -> -fullHeight },
                animationSpec = animationSpec
            ) togetherWith slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = animationSpec
            )
        }
    }
    
    /**
     * Creates a fade transition with scale for modal presentations.
     */
    fun modalTransition(
        animationSpec: FiniteAnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): ContentTransform {
        return fadeIn(animationSpec = animationSpec) + scaleIn(
            initialScale = 0.8f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = animationSpec
        ) togetherWith fadeOut(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt() / 2,
                easing = FastOutLinearInEasing
            )
        ) + scaleOut(
            targetScale = 1.1f,
            transformOrigin = TransformOrigin.Center,
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt() / 2,
                easing = FastOutLinearInEasing
            )
        )
    }
    
    /**
     * Creates a shared element transition for hero animations.
     */
    fun sharedElementTransition(
        animationSpec: FiniteAnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): ContentTransform {
        return fadeIn(animationSpec = animationSpec) + scaleIn(
            initialScale = 0.9f,
            animationSpec = animationSpec
        ) togetherWith fadeOut(animationSpec = animationSpec) + scaleOut(
            targetScale = 1.1f,
            animationSpec = animationSpec
        )
    }
    
    /**
     * Creates a bottom sheet style transition.
     */
    fun bottomSheetTransition(
        animationSpec: FiniteAnimationSpec<IntOffset> = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    ): ContentTransform {
        return slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = animationSpec
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ) togetherWith slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = animationSpec
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt() / 2,
                easing = FastOutLinearInEasing
            )
        )
    }
    
    /**
     * Creates a crossfade transition for content switching.
     */
    fun crossfadeTransition(
        animationSpec: FiniteAnimationSpec<Float> = tween(
            durationMillis = ExpressiveMotion.CoordinatedAnimationDuration.inWholeMilliseconds.toInt(),
            easing = FastOutSlowInEasing
        )
    ): ContentTransform {
        return fadeIn(animationSpec = animationSpec) togetherWith fadeOut(animationSpec = animationSpec)
    }
}

/**
 * Composable for animated screen transitions with expressive motion.
 */
@Composable
fun ExpressiveScreenTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    transitionDirection: SlideDirection = SlideDirection.Left,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "ExpressiveScreenTransition",
    content: @Composable AnimatedContentScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            ExpressiveNavigation.slideTransition(
                direction = transitionDirection,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        },
        contentAlignment = contentAlignment,
        label = label,
        content = content
    )
}

/**
 * Composable for modal screen transitions.
 */
@Composable
fun ExpressiveModalTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    label: String = "ExpressiveModalTransition",
    content: @Composable AnimatedContentScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            ExpressiveNavigation.modalTransition()
        },
        contentAlignment = contentAlignment,
        label = label,
        content = content
    )
}

/**
 * Composable for shared element transitions between screens.
 */
@Composable
fun ExpressiveSharedElementTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    label: String = "ExpressiveSharedElementTransition",
    content: @Composable AnimatedContentScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            ExpressiveNavigation.sharedElementTransition()
        },
        contentAlignment = contentAlignment,
        label = label,
        content = content
    )
}

/**
 * Composable for bottom sheet style transitions.
 */
@Composable
fun ExpressiveBottomSheetTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt() / 2,
                easing = FastOutLinearInEasing
            )
        ),
        content = { content() }
    )
}

/**
 * Composable for content crossfade transitions.
 */
@Composable
fun ExpressiveCrossfadeTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    animationSpec: FiniteAnimationSpec<Float> = tween(
        durationMillis = ExpressiveMotion.CoordinatedAnimationDuration.inWholeMilliseconds.toInt(),
        easing = FastOutSlowInEasing
    ),
    label: String = "ExpressiveCrossfadeTransition",
    content: @Composable (targetState: Any) -> Unit
) {
    Crossfade(
        targetState = targetState,
        modifier = modifier,
        animationSpec = animationSpec,
        label = label,
        content = content
    )
}

/**
 * Extension functions for common navigation patterns.
 */

/**
 * Creates a forward navigation transition (left to right slide).
 */
fun forwardTransition(): ContentTransform = ExpressiveNavigation.slideTransition(SlideDirection.Left)

/**
 * Creates a backward navigation transition (right to left slide).
 */
fun backwardTransition(): ContentTransform = ExpressiveNavigation.slideTransition(SlideDirection.Right)

/**
 * Creates an upward navigation transition (bottom to top slide).
 */
fun upwardTransition(): ContentTransform = ExpressiveNavigation.slideTransition(SlideDirection.Up)

/**
 * Creates a downward navigation transition (top to bottom slide).
 */
fun downwardTransition(): ContentTransform = ExpressiveNavigation.slideTransition(SlideDirection.Down)