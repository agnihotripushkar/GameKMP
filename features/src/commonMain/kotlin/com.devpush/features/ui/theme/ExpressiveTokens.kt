package com.devpush.features.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds

/**
 * Simple design tokens for expressive Material 3 components
 * These are used directly in components rather than complex theme classes
 */
object ExpressiveTokens {
    
    // Corner radius tokens for expressive shapes
    object CornerRadius {
        val ExtraSmall = 8.dp
        val Small = 12.dp
        val Medium = 16.dp
        val Large = 20.dp
        val ExtraLarge = 28.dp
    }
    
    // Elevation tokens for expressive shadows
    object Elevation {
        val Level0 = 0.dp
        val Level1 = 2.dp
        val Level2 = 6.dp
        val Level3 = 12.dp
        val Level4 = 16.dp
        val Level5 = 24.dp
    }
    
    // Animation duration tokens
    object Motion {
        val Quick = 200.milliseconds
        val Medium = 300.milliseconds
        val Slow = 500.milliseconds
        val ExtraSlow = 700.milliseconds
    }
    
    // Common shapes used in expressive components
    object Shapes {
        val Card = RoundedCornerShape(CornerRadius.Medium)
        val CardSmall = RoundedCornerShape(CornerRadius.Small)
        val Button = RoundedCornerShape(CornerRadius.Large)
        val TextField = RoundedCornerShape(CornerRadius.Small)
        val FAB = RoundedCornerShape(CornerRadius.Medium)
    }
    
    // FAB Menu specific tokens
    object FABMenu {
        val MainFABSize = 56.dp
        val ActionFABSize = 40.dp
        val ActionSpacing = 16.dp
        val OverlayAlpha = 0.6f
        val MinTouchTarget = 48.dp
        
        // Performance-related tokens
        val MaxConcurrentAnimations = 8
        val ReducedMotionDurationScale = 0.5f
        val MinimumAnimationDuration = 100.milliseconds
    }
    
    // Performance optimization tokens
    object Performance {
        val MaxAnimationDuration = 1000.milliseconds
        val DefaultFrameRate = 60
        val ReducedFrameRate = 30
        val MinFrameRate = 15
        
        // Memory management
        val MaxCachedAnimations = 20
        val AnimationCleanupDelay = 5000.milliseconds
    }
}