package com.devpush.features.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

/**
 * Motion choreography utilities for expressive Material 3 components.
 * Provides coordinated animations, page transitions, and entrance/exit effects.
 */
object ExpressiveMotion {
    
    /**
     * Standard duration for page transitions.
     */
    val PageTransitionDuration = 400.milliseconds
    
    /**
     * Standard duration for dialog animations.
     */
    val DialogTransitionDuration = 300.milliseconds
    
    /**
     * Standard duration for coordinated element animations.
     */
    val CoordinatedAnimationDuration = 250.milliseconds
    
    /**
     * Stagger delay for coordinated animations.
     */
    val CoordinatedStaggerDelay = 50.milliseconds
    
    /**
     * Creates a smooth page transition with proper motion choreography.
     */
    fun pageTransitionSpec(): FiniteAnimationSpec<IntOffset> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Creates a dialog entrance animation spec.
     */
    fun dialogEntranceSpec(): FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Creates a dialog exit animation spec.
     */
    fun dialogExitSpec(): FiniteAnimationSpec<Float> = tween(
        durationMillis = DialogTransitionDuration.inWholeMilliseconds.toInt(),
        easing = FastOutLinearInEasing
    )
    
    /**
     * Creates coordinated animation specs for related elements.
     */
    fun coordinatedAnimationSpec(
        index: Int,
        baseDelay: Duration = CoordinatedStaggerDelay
    ): FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh,
        visibilityThreshold = 0.01f
    )
    
    /**
     * Creates entrance animation for overlays and dialogs.
     */
    @Composable
    fun entranceAnimation(
        visible: Boolean,
        animationSpec: FiniteAnimationSpec<Float> = dialogEntranceSpec()
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = animationSpec,
            label = "entranceAnimation"
        )
    }
    
    /**
     * Creates exit animation for overlays and dialogs.
     */
    @Composable
    fun exitAnimation(
        visible: Boolean,
        animationSpec: FiniteAnimationSpec<Float> = dialogExitSpec()
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = animationSpec,
            label = "exitAnimation"
        )
    }
    
    /**
     * Creates coordinated animations for multiple related elements.
     */
    @Composable
    fun coordinatedElementAnimation(
        visible: Boolean,
        index: Int,
        staggerDelay: Duration = CoordinatedStaggerDelay
    ): State<Float> {
        var shouldAnimate by remember { mutableStateOf(false) }
        
        LaunchedEffect(visible) {
            if (visible) {
                kotlinx.coroutines.delay(staggerDelay * index)
                shouldAnimate = true
            } else {
                shouldAnimate = false
            }
        }
        
        return animateFloatAsState(
            targetValue = if (shouldAnimate) 1f else 0f,
            animationSpec = coordinatedAnimationSpec(index),
            label = "coordinatedElement_$index"
        )
    }
    
    /**
     * Creates shared element transition animations.
     */
    @Composable
    fun sharedElementTransition(
        visible: Boolean,
        transformOrigin: TransformOrigin = TransformOrigin.Center
    ): SharedElementTransitionState {
        val scale by animateFloatAsState(
            targetValue = if (visible) 1f else 0.8f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            label = "sharedElementScale"
        )
        
        val alpha by animateFloatAsState(
            targetValue = if (visible) 1f else 0f,
            animationSpec = tween(
                durationMillis = PageTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            ),
            label = "sharedElementAlpha"
        )
        
        return SharedElementTransitionState(
            scale = scale,
            alpha = alpha,
            transformOrigin = transformOrigin
        )
    }
    
    /**
     * Creates slide transition for page navigation.
     */
    fun slideTransition(
        direction: SlideDirection = SlideDirection.Left
    ): ContentTransform {
        val slideDistance = 300.dp
        val animationSpec = pageTransitionSpec()
        
        return when (direction) {
            SlideDirection.Left -> slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = animationSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = animationSpec
            )
            SlideDirection.Right -> slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = animationSpec
            ) togetherWith slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = animationSpec
            )
            SlideDirection.Up -> slideInVertically(
                initialOffsetY = { it },
                animationSpec = animationSpec
            ) togetherWith slideOutVertically(
                targetOffsetY = { -it },
                animationSpec = animationSpec
            )
            SlideDirection.Down -> slideInVertically(
                initialOffsetY = { -it },
                animationSpec = animationSpec
            ) togetherWith slideOutVertically(
                targetOffsetY = { it },
                animationSpec = animationSpec
            )
        }
    }
    
    /**
     * Creates fade transition with scale for dialogs.
     */
    fun dialogTransition(): ContentTransform {
        return fadeIn(
            animationSpec = tween(
                durationMillis = DialogTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = dialogEntranceSpec()
        ) togetherWith fadeOut(
            animationSpec = tween(
                durationMillis = (DialogTransitionDuration.inWholeMilliseconds / 2).toInt(),
                easing = FastOutLinearInEasing
            )
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = dialogExitSpec()
        )
    }
    
    /**
     * Creates expand/collapse transition for content.
     */
    fun expandTransition(): ContentTransform {
        return expandVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = CoordinatedAnimationDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ) togetherWith shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessHigh
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = (CoordinatedAnimationDuration.inWholeMilliseconds / 2).toInt(),
                easing = FastOutLinearInEasing
            )
        )
    }
}

/**
 * Direction for slide transitions.
 */
enum class SlideDirection {
    Left, Right, Up, Down
}

/**
 * State holder for shared element transitions.
 */
data class SharedElementTransitionState(
    val scale: Float,
    val alpha: Float,
    val transformOrigin: TransformOrigin
)

/**
 * Composable for animated page transitions with proper motion choreography.
 */
@Composable
fun ExpressivePageTransition(
    targetState: Any,
    modifier: Modifier = Modifier,
    transitionSpec: AnimatedContentTransitionScope<Any>.() -> ContentTransform = {
        ExpressiveMotion.slideTransition(SlideDirection.Left)
    },
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "ExpressivePageTransition",
    content: @Composable AnimatedContentScope.(targetState: Any) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = transitionSpec,
        contentAlignment = contentAlignment,
        label = label,
        content = content
    )
}

/**
 * Composable for animated dialog transitions.
 */
@Composable
fun ExpressiveDialogTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ) + scaleIn(
            initialScale = 0.8f,
            animationSpec = ExpressiveMotion.dialogEntranceSpec()
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = (ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds / 2).toInt(),
                easing = FastOutLinearInEasing
            )
        ) + scaleOut(
            targetScale = 0.8f,
            animationSpec = ExpressiveMotion.dialogExitSpec()
        ),
        content = { content() }
    )
}

/**
 * Composable for coordinated element animations.
 */
@Composable
fun ExpressiveCoordinatedAnimation(
    visible: Boolean,
    elements: List<@Composable (animationProgress: Float) -> Unit>,
    modifier: Modifier = Modifier,
    staggerDelay: Duration = ExpressiveMotion.CoordinatedStaggerDelay
) {
    Box(modifier = modifier.fillMaxSize()) {
        elements.forEachIndexed { index, element ->
            val animationProgress by ExpressiveMotion.coordinatedElementAnimation(
                visible = visible,
                index = index,
                staggerDelay = staggerDelay
            )
            
            element(animationProgress)
        }
    }
}

/**
 * Composable for overlay entrance/exit animations.
 */
@Composable
fun ExpressiveOverlayTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds.toInt(),
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = (ExpressiveMotion.DialogTransitionDuration.inWholeMilliseconds / 2).toInt(),
                easing = FastOutLinearInEasing
            )
        ),
        content = { content() }
    )
}

/**
 * Extension functions for common motion patterns.
 */

/**
 * Creates a staggered list animation.
 */
@Composable
fun <T> List<T>.animatedForEachIndexed(
    visible: Boolean,
    staggerDelay: Duration = ExpressiveMotion.CoordinatedStaggerDelay,
    content: @Composable (index: Int, item: T, animationProgress: Float) -> Unit
) {
    this.forEachIndexed { index, item ->
        val animationProgress by ExpressiveMotion.coordinatedElementAnimation(
            visible = visible,
            index = index,
            staggerDelay = staggerDelay
        )
        
        content(index, item, animationProgress)
    }
}

/**
 * Creates a coordinated fade and slide animation.
 */
@Composable
fun coordinatedFadeSlide(
    visible: Boolean,
    slideDistance: Dp = 16.dp,
    animationSpec: FiniteAnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
): Pair<Float, Float> {
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = animationSpec,
        label = "coordinatedAlpha"
    )
    
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else slideDistance.value,
        animationSpec = animationSpec,
        label = "coordinatedOffset"
    )
    
    return Pair(alpha, offset)
}