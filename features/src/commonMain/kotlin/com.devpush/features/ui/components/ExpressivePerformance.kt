package com.devpush.features.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Performance optimization utilities for expressive Material 3 components.
 * Provides reduced motion support, performance monitoring, and graceful degradation.
 */
object ExpressivePerformance {
    
    /**
     * Performance levels for adaptive animation complexity.
     */
    enum class PerformanceLevel {
        High,      // Full animations and effects
        Medium,    // Reduced complexity animations
        Low,       // Minimal animations
        Minimal    // No animations (accessibility mode)
    }
    
    /**
     * Animation complexity levels.
     */
    enum class AnimationComplexity {
        Full,      // All animations enabled
        Reduced,   // Simplified animations
        Essential, // Only essential animations
        None       // No animations
    }
    
    /**
     * Device performance characteristics.
     */
    @Stable
    data class DevicePerformance(
        val level: PerformanceLevel,
        val supportsComplexAnimations: Boolean,
        val recommendedFrameRate: Int,
        val maxConcurrentAnimations: Int
    )
    
    /**
     * Animation preferences based on system settings and performance.
     */
    @Stable
    data class AnimationPreferences(
        val isReducedMotionEnabled: Boolean,
        val complexity: AnimationComplexity,
        val durationScale: Float,
        val enableSpringAnimations: Boolean,
        val enableStaggeredAnimations: Boolean,
        val enableParallaxEffects: Boolean
    )
    
    /**
     * Gets the current device performance characteristics.
     */
    @Composable
    fun getDevicePerformance(): DevicePerformance {
        // In a real implementation, this would check device capabilities
        // For now, we'll provide reasonable defaults
        return remember {
            DevicePerformance(
                level = PerformanceLevel.High,
                supportsComplexAnimations = true,
                recommendedFrameRate = 60,
                maxConcurrentAnimations = 10
            )
        }
    }
    
    /**
     * Gets the current animation preferences based on system settings.
     */
    @Composable
    fun getAnimationPreferences(): AnimationPreferences {
        // Check for reduced motion preference
        val isReducedMotion = shouldUseReducedMotion()
        val devicePerformance = getDevicePerformance()
        
        return remember(isReducedMotion, devicePerformance) {
            when {
                isReducedMotion -> AnimationPreferences(
                    isReducedMotionEnabled = true,
                    complexity = AnimationComplexity.None,
                    durationScale = 0f,
                    enableSpringAnimations = false,
                    enableStaggeredAnimations = false,
                    enableParallaxEffects = false
                )
                devicePerformance.level == PerformanceLevel.Low -> AnimationPreferences(
                    isReducedMotionEnabled = false,
                    complexity = AnimationComplexity.Essential,
                    durationScale = 0.5f,
                    enableSpringAnimations = false,
                    enableStaggeredAnimations = false,
                    enableParallaxEffects = false
                )
                devicePerformance.level == PerformanceLevel.Medium -> AnimationPreferences(
                    isReducedMotionEnabled = false,
                    complexity = AnimationComplexity.Reduced,
                    durationScale = 0.75f,
                    enableSpringAnimations = true,
                    enableStaggeredAnimations = false,
                    enableParallaxEffects = false
                )
                else -> AnimationPreferences(
                    isReducedMotionEnabled = false,
                    complexity = AnimationComplexity.Full,
                    durationScale = 1f,
                    enableSpringAnimations = true,
                    enableStaggeredAnimations = true,
                    enableParallaxEffects = true
                )
            }
        }
    }
    
    /**
     * Checks if reduced motion should be used based on system preferences.
     */
    @Composable
    fun shouldUseReducedMotion(): Boolean {
        // In a real implementation, this would check system accessibility settings
        // For now, we'll provide a way to simulate this
        return remember { false } // Default to false for demo purposes
    }
    
    /**
     * Creates an optimized animation spec based on performance preferences.
     */
    @Composable
    fun <T> optimizedAnimationSpec(
        defaultSpec: AnimationSpec<T>,
        reducedMotionSpec: AnimationSpec<T> = snap()
    ): AnimationSpec<T> {
        val preferences = getAnimationPreferences()
        
        return when (preferences.complexity) {
            AnimationComplexity.None -> reducedMotionSpec
            AnimationComplexity.Essential -> reducedMotionSpec
            AnimationComplexity.Reduced -> defaultSpec
            AnimationComplexity.Full -> defaultSpec
        }
    }
    
    /**
     * Creates an optimized spring animation spec.
     */
    @Composable
    fun <T> optimizedSpringSpec(
        dampingRatio: Float = Spring.DampingRatioMediumBouncy,
        stiffness: Float = Spring.StiffnessMedium
    ): SpringSpec<T> {
        val preferences = getAnimationPreferences()
        
        return spring(
            dampingRatio = if (preferences.complexity == AnimationComplexity.Reduced) 
                Spring.DampingRatioNoBouncy else dampingRatio,
            stiffness = if (preferences.complexity == AnimationComplexity.Reduced) 
                Spring.StiffnessHigh else stiffness
        )
    }
    
    /**
     * Calculates optimized stagger delay based on performance preferences.
     */
    @Composable
    fun optimizedStaggerDelay(
        baseDelay: Duration = 50.milliseconds,
        itemCount: Int = 1
    ): Duration {
        val preferences = getAnimationPreferences()
        
        return when {
            !preferences.enableStaggeredAnimations -> Duration.ZERO
            preferences.complexity == AnimationComplexity.Essential -> Duration.ZERO
            preferences.complexity == AnimationComplexity.Reduced -> baseDelay.times(0.5)
            else -> baseDelay.times(preferences.durationScale.toDouble())
        }
    }
    
    /**
     * Determines if complex animations should be enabled.
     */
    @Composable
    fun shouldEnableComplexAnimations(): Boolean {
        val preferences = getAnimationPreferences()
        val devicePerformance = getDevicePerformance()
        
        return preferences.complexity == AnimationComplexity.Full && 
               devicePerformance.supportsComplexAnimations
    }
    
    /**
     * Creates a performance-aware animation state.
     */
    @Composable
    fun performanceAwareAnimateFloatAsState(
        targetValue: Float,
        animationSpec: AnimationSpec<Float> = spring(),
        label: String = "performanceAwareAnimation",
        finishedListener: ((Float) -> Unit)? = null
    ): State<Float> {
        val optimizedSpec = optimizedAnimationSpec(animationSpec)
        
        return animateFloatAsState(
            targetValue = targetValue,
            animationSpec = optimizedSpec,
            label = label,
            finishedListener = finishedListener
        )
    }
    
    /**
     * Memory management utilities for animations.
     */
    object MemoryManager {
        
        /**
         * Tracks active animations for memory management.
         */
        private val activeAnimations = mutableSetOf<String>()
        
        /**
         * Registers an animation for tracking.
         */
        fun registerAnimation(id: String) {
            activeAnimations.add(id)
        }
        
        /**
         * Unregisters an animation when completed.
         */
        fun unregisterAnimation(id: String) {
            activeAnimations.remove(id)
        }
        
        /**
         * Gets the count of active animations.
         */
        fun getActiveAnimationCount(): Int = activeAnimations.size
        
        /**
         * Clears all animation tracking (for cleanup).
         */
        fun clearAll() {
            activeAnimations.clear()
        }
        
        /**
         * Checks if the system can handle more animations.
         */
        @Composable
        fun canStartNewAnimation(): Boolean {
            val devicePerformance = getDevicePerformance()
            return getActiveAnimationCount() < devicePerformance.maxConcurrentAnimations
        }
    }
    
    /**
     * Performance monitoring utilities.
     */
    object PerformanceMonitor {
        
        /**
         * Monitors animation performance and adjusts complexity if needed.
         */
        @Composable
        fun MonitorPerformance(
            onPerformanceIssue: (PerformanceLevel) -> Unit = {}
        ) {
            val devicePerformance = getDevicePerformance()
            
            // In a real implementation, this would monitor frame rates and adjust accordingly
            LaunchedEffect(devicePerformance) {
                // Performance monitoring logic would go here
            }
        }
        
        /**
         * Creates a performance-aware disposable effect for animation cleanup.
         */
        @Composable
        fun PerformanceAwareDisposableEffect(
            key: Any?,
            effect: DisposableEffectScope.() -> DisposableEffectResult
        ) {
            DisposableEffect(key) {
                val result = effect()
                onDispose {
                    // Ensure proper cleanup
                    result.dispose()
                    // Clear any animation references
                    MemoryManager.clearAll()
                }
            }
        }
    }
    
    /**
     * Reduced motion alternatives for common animations.
     */
    object ReducedMotionAlternatives {
        
        /**
         * Creates a reduced motion alternative for fade animations.
         */
        @Composable
        fun reducedMotionFade(
            visible: Boolean,
            label: String = "reducedMotionFade"
        ): State<Float> {
            val preferences = getAnimationPreferences()
            
            return if (preferences.isReducedMotionEnabled) {
                // Instant transition for reduced motion
                remember(visible) { mutableStateOf(if (visible) 1f else 0f) }
            } else {
                animateFloatAsState(
                    targetValue = if (visible) 1f else 0f,
                    animationSpec = optimizedAnimationSpec(
                        tween(durationMillis = 200, easing = LinearEasing)
                    ),
                    label = label
                )
            }
        }
        
        /**
         * Creates a reduced motion alternative for scale animations.
         */
        @Composable
        fun reducedMotionScale(
            targetScale: Float,
            label: String = "reducedMotionScale"
        ): State<Float> {
            val preferences = getAnimationPreferences()
            
            return if (preferences.isReducedMotionEnabled) {
                // Instant transition for reduced motion
                remember(targetScale) { mutableStateOf(targetScale) }
            } else {
                animateFloatAsState(
                    targetValue = targetScale,
                    animationSpec = optimizedAnimationSpec(
                        spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    ),
                    label = label
                )
            }
        }
        
        /**
         * Creates a reduced motion alternative for rotation animations.
         */
        @Composable
        fun reducedMotionRotation(
            targetRotation: Float,
            label: String = "reducedMotionRotation"
        ): State<Float> {
            val preferences = getAnimationPreferences()
            
            return if (preferences.isReducedMotionEnabled) {
                // Instant transition for reduced motion
                remember(targetRotation) { mutableStateOf(targetRotation) }
            } else {
                animateFloatAsState(
                    targetValue = targetRotation,
                    animationSpec = optimizedAnimationSpec(
                        tween(durationMillis = 300, easing = FastOutSlowInEasing)
                    ),
                    label = label
                )
            }
        }
    }
}

/**
 * Composable function to provide performance context to child components.
 */
@Composable
fun PerformanceAwareContent(
    content: @Composable (ExpressivePerformance.AnimationPreferences) -> Unit
) {
    val preferences = ExpressivePerformance.getAnimationPreferences()
    
    // Monitor performance
    ExpressivePerformance.PerformanceMonitor.MonitorPerformance()
    
    content(preferences)
}

/**
 * Extension functions for performance-aware animations.
 */

/**
 * Creates a performance-aware float animation.
 */
@Composable
fun animatePerformanceAwareFloat(
    targetValue: Float,
    animationSpec: AnimationSpec<Float> = spring(),
    label: String = "performanceAwareFloat"
): State<Float> {
    return ExpressivePerformance.performanceAwareAnimateFloatAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}

/**
 * Creates a performance-aware Dp animation.
 */
@Composable
fun animatePerformanceAwareDp(
    targetValue: androidx.compose.ui.unit.Dp,
    animationSpec: AnimationSpec<androidx.compose.ui.unit.Dp> = spring(),
    label: String = "performanceAwareDp"
): State<androidx.compose.ui.unit.Dp> {
    return animateDpAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}

/**
 * Creates a performance-aware Color animation.
 */
@Composable
fun animatePerformanceAwareColor(
    targetValue: androidx.compose.ui.graphics.Color,
    animationSpec: AnimationSpec<androidx.compose.ui.graphics.Color> = tween(),
    label: String = "performanceAwareColor"
): State<androidx.compose.ui.graphics.Color> {
    return animateColorAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        label = label
    )
}