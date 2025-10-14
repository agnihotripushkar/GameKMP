package com.devpush.kmp.ui.layout

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.*
import com.devpush.kmp.ui.utils.ConstraintValidator
import com.devpush.kmp.ui.utils.ConstraintHelpers
import kotlin.test.*

/**
 * Comprehensive unit tests for constraint validation logic.
 * Tests the core validation algorithms and constraint checking utilities.
 */
class LayoutValidationTest {

    @Test
    fun `constraint validation should detect all infinite constraint scenarios`() {
        // Test infinite height
        val infiniteHeightConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val heightResult = ConstraintValidator.validateConstraints("TestComponent", infiniteHeightConstraints)
        assertFalse(heightResult.isValid)
        assertTrue(heightResult.issues.any { it.issueType == ConstraintIssueType.INFINITE_HEIGHT })

        // Test infinite width
        val infiniteWidthConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val widthResult = ConstraintValidator.validateConstraints("TestComponent", infiniteWidthConstraints)
        assertFalse(widthResult.isValid)
        assertTrue(widthResult.issues.any { it.issueType == ConstraintIssueType.INFINITE_WIDTH })

        // Test both infinite
        val bothInfiniteConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        val bothResult = ConstraintValidator.validateConstraints("TestComponent", bothInfiniteConstraints)
        assertFalse(bothResult.isValid)
        assertTrue(bothResult.issues.any { it.issueType == ConstraintIssueType.INFINITE_HEIGHT })
        assertTrue(bothResult.issues.any { it.issueType == ConstraintIssueType.INFINITE_WIDTH })
    }

    @Test
    fun `constraint validation should detect zero constraint scenarios`() {
        val zeroHeightConstraints = Constraints(0, 1080, 0, 0)
        val heightResult = ConstraintValidator.validateConstraints("TestComponent", zeroHeightConstraints)
        assertFalse(heightResult.isValid)
        assertTrue(heightResult.issues.any { it.issueType == ConstraintIssueType.ZERO_HEIGHT })

        val zeroWidthConstraints = Constraints(0, 0, 0, 1920)
        val widthResult = ConstraintValidator.validateConstraints("TestComponent", zeroWidthConstraints)
        assertFalse(widthResult.isValid)
        assertTrue(widthResult.issues.any { it.issueType == ConstraintIssueType.ZERO_WIDTH })

        val bothZeroConstraints = Constraints(0, 0, 0, 0)
        val bothResult = ConstraintValidator.validateConstraints("TestComponent", bothZeroConstraints)
        assertFalse(bothResult.isValid)
        assertTrue(bothResult.issues.any { it.issueType == ConstraintIssueType.ZERO_HEIGHT })
        assertTrue(bothResult.issues.any { it.issueType == ConstraintIssueType.ZERO_WIDTH })
    }

    @Test
    fun `constraint validation should pass for valid constraints`() {
        val validConstraints = Constraints(0, 1080, 0, 1920)
        val result = ConstraintValidator.validateConstraints("TestComponent", validConstraints)
        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun `scrollable constraint validation should detect critical issues for LazyColumn`() {
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyColumn",
            problematicConstraints,
            ScrollDirection.Vertical
        )
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `scrollable constraint validation should detect critical issues for LazyRow`() {
        val problematicConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyRow",
            problematicConstraints,
            ScrollDirection.Horizontal
        )
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_WIDTH && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `scrollable constraint validation should detect critical issues for LazyVerticalGrid`() {
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val result = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            problematicConstraints,
            isVertical = true
        )
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `scrollable constraint validation should detect critical issues for LazyHorizontalGrid`() {
        val problematicConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val result = ConstraintValidator.validateLazyGridConstraints(
            "LazyHorizontalGrid",
            problematicConstraints,
            isVertical = false
        )
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_WIDTH && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `anti-pattern detection should identify nested scrollable components`() {
        val infiniteConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "LazyColumn",
            infiniteConstraints,
            ScrollDirection.Vertical
        )
        
        assertTrue(antiPatterns.any { 
            it.name == "Infinite Constraint Scrollable" && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `anti-pattern detection should identify double infinite constraints`() {
        val doubleInfiniteConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "TestComponent",
            doubleInfiniteConstraints
        )
        
        assertTrue(antiPatterns.any { it.name == "Double Infinite Constraints" })
    }

    @Test
    fun `pull-to-refresh constraint validation should detect problematic scenarios`() {
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            "PullToRefreshBox",
            problematicConstraints
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.severity == IssueSeverity.CRITICAL })
        assertTrue(result.recommendations.any { it.contains("SafePullToRefreshBox") })
    }

    @Test
    fun `constraint helpers should correctly identify safe scrolling scenarios`() {
        val safeVerticalConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeVerticalConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val safeHorizontalConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeHorizontalConstraints = Constraints(0, Constraints.Infinity, 0, 1920)

        assertTrue(ConstraintHelpers.areConstraintsSafeForScrolling(safeVerticalConstraints, ScrollDirection.Vertical))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeVerticalConstraints, ScrollDirection.Vertical))
        assertTrue(ConstraintHelpers.areConstraintsSafeForScrolling(safeHorizontalConstraints, ScrollDirection.Horizontal))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeHorizontalConstraints, ScrollDirection.Horizontal))
    }

    @Test
    fun `constraint helpers should detect crash scenarios correctly`() {
        val crashConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val safeConstraints = Constraints(0, 1080, 0, 1920)

        assertTrue(ConstraintHelpers.willCauseScrollableCrash(crashConstraints, ScrollDirection.Vertical))
        assertFalse(ConstraintHelpers.willCauseScrollableCrash(safeConstraints, ScrollDirection.Vertical))
    }

    @Test
    fun `safe fallback constraints should provide valid alternatives`() {
        val infiniteConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        val safeConstraints = ConstraintValidator.getSafeFallbackConstraints(
            infiniteConstraints,
            fallbackHeight = 1200,
            fallbackWidth = 1080
        )

        assertEquals(1080, safeConstraints.maxWidth)
        assertEquals(1200, safeConstraints.maxHeight)
        assertTrue(safeConstraints.maxWidth != Constraints.Infinity)
        assertTrue(safeConstraints.maxHeight != Constraints.Infinity)
    }

    @Test
    fun `constraint validation should provide appropriate recommendations`() {
        val problematicConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyColumn",
            problematicConstraints,
            ScrollDirection.Vertical
        )

        assertTrue(result.recommendations.any { it.contains("weight") })
        assertTrue(result.recommendations.any { it.contains("ConstrainedScrollableContainer") })
        assertTrue(result.recommendations.any { it.contains("finite height") })
    }

    @Test
    fun `constraint validation should handle edge cases`() {
        // Test very large but finite constraints
        val largeConstraints = Constraints(0, Int.MAX_VALUE - 1, 0, Int.MAX_VALUE - 1)
        val largeResult = ConstraintValidator.validateConstraints("TestComponent", largeConstraints)
        assertTrue(largeResult.isValid)

        // Test minimum constraints
        val minConstraints = Constraints(1, 1, 1, 1)
        val minResult = ConstraintValidator.validateConstraints("TestComponent", minConstraints)
        assertTrue(minResult.isValid)

        // Test asymmetric constraints
        val asymmetricConstraints = Constraints(100, 200, 50, 150)
        val asymmetricResult = ConstraintValidator.validateConstraints("TestComponent", asymmetricConstraints)
        assertTrue(asymmetricResult.isValid)
    }

    @Test
    fun `constraint validation performance should be acceptable`() {
        val constraints = Constraints(0, 1080, 0, Constraints.Infinity)
        
        val startTime = kotlin.system.getTimeMillis()
        repeat(1000) {
            ConstraintValidator.validateConstraints("TestComponent", constraints)
        }
        val endTime = kotlin.system.getTimeMillis()
        
        val duration = endTime - startTime
        assertTrue(duration < 100, "Constraint validation took too long: ${duration}ms for 1000 iterations")
    }
}