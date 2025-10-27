package com.devpush.features.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.devpush.features.ui.theme.ExpressiveTokens

/**
 * Responsive design utilities for expressive Material 3 components.
 * Provides adaptive layouts and sizing for different screen sizes and device types.
 */
object ExpressiveResponsive {
    
    /**
     * Screen size breakpoints following Material 3 guidelines.
     */
    object Breakpoints {
        val Compact = 600.dp
        val Medium = 840.dp
        val Expanded = 1200.dp
    }
    
    /**
     * Device type classifications based on screen size.
     */
    enum class DeviceType {
        Phone,
        Tablet,
        Desktop
    }
    
    /**
     * Screen size classifications.
     */
    enum class ScreenSize {
        Compact,
        Medium,
        Expanded
    }
    
    /**
     * Orientation classifications.
     */
    enum class Orientation {
        Portrait,
        Landscape
    }
    
    /**
     * Gets the current device type based on screen dimensions.
     */
    @Composable
    fun getDeviceType(): DeviceType {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < Breakpoints.Compact -> DeviceType.Phone
            screenWidth < Breakpoints.Medium -> DeviceType.Tablet
            else -> DeviceType.Desktop
        }
    }
    
    /**
     * Gets the current screen size classification.
     */
    @Composable
    fun getScreenSize(): ScreenSize {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        
        return when {
            screenWidth < Breakpoints.Compact -> ScreenSize.Compact
            screenWidth < Breakpoints.Medium -> ScreenSize.Medium
            else -> ScreenSize.Expanded
        }
    }
    
    /**
     * Gets the current orientation.
     */
    @Composable
    fun getOrientation(): Orientation {
        val configuration = LocalConfiguration.current
        return if (configuration.screenWidthDp > configuration.screenHeightDp) {
            Orientation.Landscape
        } else {
            Orientation.Portrait
        }
    }
    
    /**
     * Calculates adaptive spacing based on screen size.
     */
    @Composable
    fun adaptiveSpacing(
        compact: Dp = 8.dp,
        medium: Dp = 12.dp,
        expanded: Dp = 16.dp
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compact
            ScreenSize.Medium -> medium
            ScreenSize.Expanded -> expanded
        }
    }
    
    /**
     * Calculates adaptive padding based on screen size.
     */
    @Composable
    fun adaptivePadding(
        compact: Dp = 16.dp,
        medium: Dp = 24.dp,
        expanded: Dp = 32.dp
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compact
            ScreenSize.Medium -> medium
            ScreenSize.Expanded -> expanded
        }
    }
    
    /**
     * Calculates adaptive FAB size based on screen size and device type.
     */
    @Composable
    fun adaptiveFABSize(
        compactSize: Dp = 56.dp,
        mediumSize: Dp = 64.dp,
        expandedSize: Dp = 72.dp
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compactSize
            ScreenSize.Medium -> mediumSize
            ScreenSize.Expanded -> expandedSize
        }
    }
    
    /**
     * Calculates adaptive action FAB size based on screen size.
     */
    @Composable
    fun adaptiveActionFABSize(
        compactSize: Dp = 40.dp,
        mediumSize: Dp = 48.dp,
        expandedSize: Dp = 56.dp
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compactSize
            ScreenSize.Medium -> mediumSize
            ScreenSize.Expanded -> expandedSize
        }
    }
    
    /**
     * Calculates adaptive corner radius based on screen size.
     */
    @Composable
    fun adaptiveCornerRadius(
        compact: Dp = ExpressiveTokens.CornerRadius.Small,
        medium: Dp = ExpressiveTokens.CornerRadius.Medium,
        expanded: Dp = ExpressiveTokens.CornerRadius.Large
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compact
            ScreenSize.Medium -> medium
            ScreenSize.Expanded -> expanded
        }
    }
    
    /**
     * Calculates adaptive elevation based on screen size.
     */
    @Composable
    fun adaptiveElevation(
        compact: Dp = ExpressiveTokens.Elevation.Level2,
        medium: Dp = ExpressiveTokens.Elevation.Level3,
        expanded: Dp = ExpressiveTokens.Elevation.Level4
    ): Dp {
        return when (getScreenSize()) {
            ScreenSize.Compact -> compact
            ScreenSize.Medium -> medium
            ScreenSize.Expanded -> expanded
        }
    }
    
    /**
     * Gets safe area insets for proper content positioning.
     */
    @Composable
    fun getSafeAreaInsets(): SafeAreaInsets {
        val density = LocalDensity.current
        val layoutDirection = LocalLayoutDirection.current
        val statusBarInsets = WindowInsets.statusBars.asPaddingValues()
        val navigationBarInsets = WindowInsets.navigationBars.asPaddingValues()
        val systemBarInsets = WindowInsets.systemBars.asPaddingValues()
        
        return SafeAreaInsets(
            top = statusBarInsets.calculateTopPadding(),
            bottom = navigationBarInsets.calculateBottomPadding(),
            start = systemBarInsets.calculateLeftPadding(layoutDirection),
            end = systemBarInsets.calculateRightPadding(layoutDirection)
        )
    }
    
    /**
     * Calculates adaptive FAB menu positioning based on screen size and safe areas.
     */
    @Composable
    fun adaptiveFABMenuPadding(): FABMenuPadding {
        val safeAreaInsets = getSafeAreaInsets()
        val basePadding = adaptivePadding()
        
        return FABMenuPadding(
            bottom = basePadding + safeAreaInsets.bottom,
            end = basePadding + safeAreaInsets.end,
            top = basePadding + safeAreaInsets.top,
            start = basePadding + safeAreaInsets.start
        )
    }
    
    /**
     * Determines if the current layout should use compact design.
     */
    @Composable
    fun isCompactLayout(): Boolean {
        return getScreenSize() == ScreenSize.Compact
    }
    
    /**
     * Determines if the current layout should use expanded design.
     */
    @Composable
    fun isExpandedLayout(): Boolean {
        return getScreenSize() == ScreenSize.Expanded
    }
    
    /**
     * Calculates the maximum number of FAB actions to display based on screen size.
     */
    @Composable
    fun maxFABActions(): Int {
        return when (getScreenSize()) {
            ScreenSize.Compact -> 4
            ScreenSize.Medium -> 6
            ScreenSize.Expanded -> 8
        }
    }
    
    /**
     * Determines if FAB actions should show labels based on screen size.
     */
    @Composable
    fun shouldShowFABActionLabels(): Boolean {
        return when (getScreenSize()) {
            ScreenSize.Compact -> true // Always show labels on small screens for clarity
            ScreenSize.Medium -> true
            ScreenSize.Expanded -> true
        }
    }
    
    /**
     * Calculates adaptive touch target size ensuring accessibility compliance.
     */
    @Composable
    fun adaptiveTouchTargetSize(
        baseSize: Dp,
        minSize: Dp = ExpressiveAccessibility.MinTouchTargetSize
    ): Dp {
        val adaptiveSize = when (getScreenSize()) {
            ScreenSize.Compact -> baseSize
            ScreenSize.Medium -> baseSize * 1.1f
            ScreenSize.Expanded -> baseSize * 1.2f
        }
        
        return maxOf(adaptiveSize, minSize)
    }
}

/**
 * Data class representing safe area insets.
 */
@Stable
data class SafeAreaInsets(
    val top: Dp,
    val bottom: Dp,
    val start: Dp,
    val end: Dp
)

/**
 * Data class representing FAB menu padding values.
 */
@Stable
data class FABMenuPadding(
    val top: Dp,
    val bottom: Dp,
    val start: Dp,
    val end: Dp
)

/**
 * Responsive configuration for FAB menu components.
 */
@Stable
data class ResponsiveFABConfig(
    val mainFABSize: Dp,
    val actionFABSize: Dp,
    val spacing: Dp,
    val padding: FABMenuPadding,
    val cornerRadius: Dp,
    val elevation: Dp,
    val maxActions: Int,
    val showLabels: Boolean
) {
    companion object {
        /**
         * Creates a responsive FAB configuration based on current screen size.
         */
        @Composable
        fun adaptive(): ResponsiveFABConfig {
            return ResponsiveFABConfig(
                mainFABSize = ExpressiveResponsive.adaptiveFABSize(),
                actionFABSize = ExpressiveResponsive.adaptiveActionFABSize(),
                spacing = ExpressiveResponsive.adaptiveSpacing(),
                padding = ExpressiveResponsive.adaptiveFABMenuPadding(),
                cornerRadius = ExpressiveResponsive.adaptiveCornerRadius(),
                elevation = ExpressiveResponsive.adaptiveElevation(),
                maxActions = ExpressiveResponsive.maxFABActions(),
                showLabels = ExpressiveResponsive.shouldShowFABActionLabels()
            )
        }
    }
}

/**
 * Utility functions for responsive design calculations.
 */
object ResponsiveUtils {
    
    /**
     * Calculates a responsive value based on screen width.
     */
    @Composable
    fun <T> responsiveValue(
        compact: T,
        medium: T,
        expanded: T
    ): T {
        return when (ExpressiveResponsive.getScreenSize()) {
            ExpressiveResponsive.ScreenSize.Compact -> compact
            ExpressiveResponsive.ScreenSize.Medium -> medium
            ExpressiveResponsive.ScreenSize.Expanded -> expanded
        }
    }
    
    /**
     * Calculates a responsive Dp value with interpolation.
     */
    @Composable
    fun responsiveDp(
        compactDp: Dp,
        expandedDp: Dp
    ): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val compactWidth = ExpressiveResponsive.Breakpoints.Compact
        val expandedWidth = ExpressiveResponsive.Breakpoints.Expanded
        
        return when {
            screenWidth <= compactWidth -> compactDp
            screenWidth >= expandedWidth -> expandedDp
            else -> {
                // Linear interpolation between compact and expanded
                val progress = (screenWidth - compactWidth) / (expandedWidth - compactWidth)
                compactDp + (expandedDp - compactDp) * progress
            }
        }
    }
    
    /**
     * Calculates responsive spacing with smooth scaling.
     */
    @Composable
    fun responsiveSpacing(
        baseSpacing: Dp = 16.dp,
        scaleFactor: Float = 1.5f
    ): Dp {
        val screenSize = ExpressiveResponsive.getScreenSize()
        return when (screenSize) {
            ExpressiveResponsive.ScreenSize.Compact -> baseSpacing
            ExpressiveResponsive.ScreenSize.Medium -> baseSpacing * 1.25f
            ExpressiveResponsive.ScreenSize.Expanded -> baseSpacing * scaleFactor
        }
    }
    
    /**
     * Determines if the current screen supports hover interactions.
     */
    @Composable
    fun supportsHover(): Boolean {
        // This would typically check for mouse/trackpad presence
        // For now, we'll assume larger screens support hover
        return ExpressiveResponsive.getScreenSize() != ExpressiveResponsive.ScreenSize.Compact
    }
    
    /**
     * Calculates the optimal number of columns for a grid layout.
     */
    @Composable
    fun optimalColumnCount(
        minItemWidth: Dp = 200.dp,
        maxColumns: Int = 4
    ): Int {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val availableWidth = screenWidth - ExpressiveResponsive.adaptivePadding() * 2
        
        val calculatedColumns = (availableWidth / minItemWidth).toInt()
        return calculatedColumns.coerceIn(1, maxColumns)
    }
}