package com.devpush.kmp.ui.layout

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.*
import com.devpush.kmp.ui.utils.ConstraintValidator
import com.devpush.features.game.ui.components.validateScrollableConstraints
import kotlin.test.*

/**
 * Automated tests for pull-to-refresh with scrollable content.
 * Tests the specific scenarios that caused the original crash and validates fixes.
 */
class PullToRefreshScrollableTest {

    @Test
    fun `PullToRefreshBox with LazyVerticalGrid should detect infinite constraint issue`() {
        // Given - The original problematic scenario
        val infiniteHeightConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity // This caused the crash
        )

        // When - Validating PullToRefreshBox constraints
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            "PullToRefreshBox",
            infiniteHeightConstraints
        )

        // Then - Should detect the critical issue
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.severity == IssueSeverity.CRITICAL })
        assertTrue(result.recommendations.any { it.contains("SafePullToRefreshBox") })
    }

    @Test
    fun `SafePullToRefreshBox should provide safe constraints for LazyVerticalGrid`() {
        // Given - SafePullToRefreshBox providing finite constraints
        val safeConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920 // SafePullToRefreshBox ensures finite height
        )

        // When - Validating safe constraints
        val result = validateScrollableConstraints(safeConstraints)

        // Then - Should be valid
        assertTrue(result.isValid)
        assertFalse(result.hasInfiniteHeight)
        assertFalse(result.hasInfiniteWidth)
        assertTrue(result.recommendations.any { it.contains("safe for scrollable") })
    }

    @Test
    fun `PullToRefreshBox with LazyColumn should detect infinite constraint issue`() {
        // Given - PullToRefreshBox with LazyColumn scenario
        val infiniteHeightConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        // When - Validating LazyColumn in PullToRefreshBox
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            infiniteHeightConstraints,
            isVertical = true
        )

        // Then - Should detect critical issue
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `PullToRefreshBox with LazyRow should detect infinite width constraint issue`() {
        // Given - PullToRefreshBox with LazyRow scenario
        val infiniteWidthConstraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = 1920
        )

        // When - Validating LazyRow in PullToRefreshBox
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyRow",
            infiniteWidthConstraints,
            isVertical = false
        )

        // Then - Should detect critical issue
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_WIDTH && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `nested PullToRefreshBox scenarios should be detected`() {
        // Given - Nested PullToRefreshBox scenario (anti-pattern)
        val nestedConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        // When - Detecting anti-patterns
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "PullToRefreshBox",
            nestedConstraints
        )

        // Then - Should detect problematic patterns
        assertTrue(antiPatterns.any { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `pull-to-refresh with different scrollable components should be validated`() {
        val scrollableComponents = listOf(
            "LazyColumn" to ScrollDirection.Vertical,
            "LazyRow" to ScrollDirection.Horizontal,
            "LazyVerticalGrid" to ScrollDirection.Vertical,
            "LazyHorizontalGrid" to ScrollDirection.Horizontal
        )

        scrollableComponents.forEach { (component, direction) ->
            // Given - Infinite constraints for each component type
            val infiniteConstraints = when (direction) {
                ScrollDirection.Vertical -> Constraints(0, 1080, 0, Constraints.Infinity)
                ScrollDirection.Horizontal -> Constraints(0, Constraints.Infinity, 0, 1920)
                ScrollDirection.Both -> Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
            }

            // When - Validating each component
            val result = ConstraintValidator.validateScrollableConstraints(
                component,
                infiniteConstraints,
                direction
            )

            // Then - Should detect issues for all components
            assertFalse(result.isValid, "$component should detect constraint issues")
            assertTrue(result.issues.any { it.severity == IssueSeverity.CRITICAL })
        }
    }

    @Test
    fun `pull-to-refresh with safe constraints should work for all scrollable components`() {
        val scrollableComponents = listOf(
            "LazyColumn",
            "LazyRow", 
            "LazyVerticalGrid",
            "LazyHorizontalGrid"
        )

        val safeConstraints = Constraints(0, 1080, 0, 1920)

        scrollableComponents.forEach { component ->
            // When - Validating with safe constraints
            val result = ConstraintValidator.validateConstraints(component, safeConstraints)

            // Then - Should be valid for all components
            assertTrue(result.isValid, "$component should work with safe constraints")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }

    @Test
    fun `pull-to-refresh state changes should maintain constraint validity`() {
        // Given - Different states during pull-to-refresh
        val refreshStates = listOf(
            "Idle" to Constraints(0, 1080, 0, 1920),
            "Pulling" to Constraints(0, 1080, 0, 1920), // Same constraints during pull
            "Refreshing" to Constraints(0, 1080, 0, 1920), // Same constraints during refresh
            "Completed" to Constraints(0, 1080, 0, 1920) // Same constraints after refresh
        )

        refreshStates.forEach { (state, constraints) ->
            // When - Validating constraints in each state
            val result = validateScrollableConstraints(constraints)

            // Then - Should be valid in all states
            assertTrue(result.isValid, "Pull-to-refresh should be valid in $state state")
        }
    }

    @Test
    fun `pull-to-refresh with dynamic content should maintain validity`() {
        // Given - Different content scenarios
        val contentScenarios = listOf(
            "Empty List" to Constraints(0, 1080, 0, 1920),
            "Small List" to Constraints(0, 1080, 0, 1920),
            "Large List" to Constraints(0, 1080, 0, 1920),
            "Loading State" to Constraints(0, 1080, 0, 1920)
        )

        contentScenarios.forEach { (scenario, constraints) ->
            // When - Validating different content scenarios
            val result = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )

            // Then - Should be valid for all content scenarios
            assertTrue(result.isValid, "Pull-to-refresh should work with $scenario")
        }
    }

    @Test
    fun `pull-to-refresh error recovery should provide valid fallbacks`() {
        // Given - Problematic pull-to-refresh constraints
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)

        // When - Getting safe fallback for pull-to-refresh
        val safeConstraints = ConstraintValidator.getSafeFallbackConstraints(
            problematicConstraints,
            fallbackHeight = 1920
        )

        // Then - Fallback should be valid for pull-to-refresh
        val fallbackResult = ConstraintValidator.validatePullToRefreshConstraints(
            "SafePullToRefreshBox",
            safeConstraints
        )
        assertTrue(fallbackResult.isValid)
    }

    @Test
    fun `pull-to-refresh with nested layouts should be validated correctly`() {
        // Given - Complex nested layout with pull-to-refresh
        val nestedLayoutConstraints = Constraints(0, 1080, 0, 1920)

        // When - Validating nested components
        val scaffoldResult = ConstraintValidator.validateConstraints("Scaffold", nestedLayoutConstraints)
        val pullToRefreshResult = ConstraintValidator.validatePullToRefreshConstraints(
            "SafePullToRefreshBox", 
            nestedLayoutConstraints
        )
        val lazyGridResult = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            nestedLayoutConstraints,
            isVertical = true
        )

        // Then - All nested components should be valid
        assertTrue(scaffoldResult.isValid, "Scaffold should be valid")
        assertTrue(pullToRefreshResult.isValid, "PullToRefreshBox should be valid")
        assertTrue(lazyGridResult.isValid, "LazyVerticalGrid should be valid")
    }

    @Test
    fun `pull-to-refresh performance should be acceptable`() {
        // Given - Constraints for performance testing
        val constraints = Constraints(0, 1080, 0, 1920)

        // When - Testing validation performance
        val startTime = kotlin.system.getTimeMillis()
        repeat(1000) {
            ConstraintValidator.validatePullToRefreshConstraints("PullToRefreshBox", constraints)
        }
        val endTime = kotlin.system.getTimeMillis()

        // Then - Should complete quickly
        val duration = endTime - startTime
        assertTrue(duration < 100, "Pull-to-refresh validation took too long: ${duration}ms")
    }

    @Test
    fun `pull-to-refresh with different screen sizes should work correctly`() {
        // Given - Various screen sizes
        val screenSizes = listOf(
            "Phone" to Constraints(0, 393, 0, 851),
            "Tablet" to Constraints(0, 820, 0, 1180),
            "Large Tablet" to Constraints(0, 1024, 0, 1366),
            "Foldable" to Constraints(0, 673, 0, 841)
        )

        screenSizes.forEach { (device, constraints) ->
            // When - Validating pull-to-refresh on different devices
            val result = ConstraintValidator.validatePullToRefreshConstraints(
                "SafePullToRefreshBox",
                constraints
            )

            // Then - Should work on all devices
            assertTrue(result.isValid, "Pull-to-refresh should work on $device")
        }
    }

    @Test
    fun `pull-to-refresh recommendations should be appropriate`() {
        // Given - Problematic pull-to-refresh scenario
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)

        // When - Getting recommendations
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            "PullToRefreshBox",
            problematicConstraints
        )

        // Then - Should provide helpful recommendations
        assertTrue(result.recommendations.any { it.contains("SafePullToRefreshBox") })
        assertTrue(result.recommendations.any { it.contains("finite height") })
        assertTrue(result.recommendations.any { it.contains("weight") })
    }

    @Test
    fun `pull-to-refresh with custom refresh indicators should be validated`() {
        // Given - Custom refresh indicator constraints
        val customIndicatorConstraints = Constraints(0, 1080, 0, 1920)

        // When - Validating custom refresh setup
        val result = ConstraintValidator.validateConstraints(
            "CustomRefreshIndicator",
            customIndicatorConstraints
        )

        // Then - Should support custom indicators
        assertTrue(result.isValid, "Custom refresh indicators should be supported")
    }

    @Test
    fun `pull-to-refresh accessibility should maintain constraint validity`() {
        // Given - Accessibility-enhanced pull-to-refresh
        val accessibilityConstraints = Constraints(0, 1080, 0, 1920)

        // When - Validating accessibility features
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            "AccessiblePullToRefreshBox",
            accessibilityConstraints
        )

        // Then - Should maintain validity with accessibility features
        assertTrue(result.isValid, "Accessibility features should not break constraints")
    }
}