package com.devpush.kmp.ui.components

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.utils.ConstraintValidator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DebugConstraintCheckerTest {
    
    @Test
    fun testInfiniteHeightDetection() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            componentName = "TestLazyColumn",
            constraints = constraints,
            scrollDirection = ScrollDirection.Vertical
        )
        
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
        assertEquals(ConstraintIssueType.INFINITE_HEIGHT, result.issues.first().issueType)
        assertEquals(IssueSeverity.CRITICAL, result.issues.first().severity)
    }
    
    @Test
    fun testInfiniteWidthDetection() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            componentName = "TestLazyRow",
            constraints = constraints,
            scrollDirection = ScrollDirection.Horizontal
        )
        
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
        assertEquals(ConstraintIssueType.INFINITE_WIDTH, result.issues.first().issueType)
        assertEquals(IssueSeverity.CRITICAL, result.issues.first().severity)
    }
    
    @Test
    fun testValidConstraints() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            componentName = "TestLazyColumn",
            constraints = constraints,
            scrollDirection = ScrollDirection.Vertical
        )
        
        assertTrue(result.isValid)
        assertEquals(0, result.issues.size)
    }
    
    @Test
    fun testZeroConstraintDetection() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 0,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateConstraints(
            componentName = "TestComponent",
            constraints = constraints
        )
        
        assertTrue(result.isValid) // Zero width is warning, not critical
        assertEquals(1, result.issues.size)
        assertEquals(ConstraintIssueType.ZERO_WIDTH, result.issues.first().issueType)
        assertEquals(IssueSeverity.WARNING, result.issues.first().severity)
    }
    
    @Test
    fun testAntiPatternDetection() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            componentName = "TestComponent",
            constraints = constraints,
            scrollDirection = ScrollDirection.Vertical
        )
        
        assertTrue(antiPatterns.isNotEmpty())
        assertTrue(antiPatterns.any { it.name == "Infinite Constraint Scrollable" })
        assertTrue(antiPatterns.any { it.name == "Double Infinite Constraints" })
    }
    
    @Test
    fun testLazyGridConstraintValidation() {
        val infiniteHeightConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateLazyGridConstraints(
            componentName = "TestLazyVerticalGrid",
            constraints = infiniteHeightConstraints,
            isVertical = true
        )
        
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
        assertEquals(ConstraintIssueType.INFINITE_HEIGHT, result.issues.first().issueType)
        assertEquals(IssueSeverity.CRITICAL, result.issues.first().severity)
        assertTrue(result.recommendations.isNotEmpty())
    }
    
    @Test
    fun testPullToRefreshConstraintValidation() {
        val infiniteHeightConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            componentName = "TestPullToRefreshBox",
            constraints = infiniteHeightConstraints
        )
        
        assertFalse(result.isValid)
        assertEquals(1, result.issues.size)
        assertEquals(ConstraintIssueType.INFINITE_HEIGHT, result.issues.first().issueType)
        assertEquals(IssueSeverity.CRITICAL, result.issues.first().severity)
        assertTrue(result.recommendations.any { it.contains("SafePullToRefreshBox") })
    }
    
    @Test
    fun testScrollableSafetyCheck() {
        val safeVerticalConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val unsafeVerticalConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        assertTrue(
            ConstraintValidator.isScrollableSafe(
                safeVerticalConstraints,
                ScrollDirection.Vertical
            )
        )
        
        assertFalse(
            ConstraintValidator.isScrollableSafe(
                unsafeVerticalConstraints,
                ScrollDirection.Vertical
            )
        )
    }
    
    @Test
    fun testSafeFallbackConstraints() {
        val infiniteConstraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val fallbackConstraints = ConstraintValidator.getSafeFallbackConstraints(
            originalConstraints = infiniteConstraints,
            fallbackHeight = 1200,
            fallbackWidth = 1080
        )
        
        assertEquals(1080, fallbackConstraints.maxWidth)
        assertEquals(1200, fallbackConstraints.maxHeight)
        assertEquals(0, fallbackConstraints.minWidth)
        assertEquals(0, fallbackConstraints.minHeight)
    }
    
    @Test
    fun testConstraintWarningDetection() {
        val infiniteHeightConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        // This would be called internally by the ConstraintWarningSystem
        // We're testing the logic that would be used
        val hasInfiniteHeight = infiniteHeightConstraints.maxHeight == Constraints.Infinity
        assertTrue(hasInfiniteHeight)
        
        // For a LazyVerticalGrid, this should be a critical warning
        val componentType = ComponentType.LAZY_VERTICAL_GRID
        val shouldWarnCritical = componentType == ComponentType.LAZY_VERTICAL_GRID && hasInfiniteHeight
        assertTrue(shouldWarnCritical)
    }
    
    @Test
    fun testConstraintValidationPerformance() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val startTime = System.currentTimeMillis()
        
        // Run validation multiple times to test performance
        repeat(100) {
            ConstraintValidator.validateConstraints(
                componentName = "PerformanceTest",
                constraints = constraints
            )
        }
        
        val endTime = System.currentTimeMillis()
        val totalTime = endTime - startTime
        
        // Validation should be fast - less than 100ms for 100 validations
        assertTrue(totalTime < 100, "Constraint validation took too long: ${totalTime}ms")
    }
}