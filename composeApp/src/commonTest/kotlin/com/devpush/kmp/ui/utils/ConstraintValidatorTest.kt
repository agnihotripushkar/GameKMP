package com.devpush.kmp.ui.utils

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.*
import kotlin.test.*

class ConstraintValidatorTest {
    
    @Test
    fun `validateConstraints should detect infinite height`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateConstraints("TestComponent", constraints)
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.issueType == ConstraintIssueType.INFINITE_HEIGHT })
    }
    
    @Test
    fun `validateConstraints should detect infinite width`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateConstraints("TestComponent", constraints)
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.issueType == ConstraintIssueType.INFINITE_WIDTH })
    }
    
    @Test
    fun `validateConstraints should detect zero constraints`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 0,
            minHeight = 0,
            maxHeight = 0
        )
        
        val result = ConstraintValidator.validateConstraints("TestComponent", constraints)
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.issueType == ConstraintIssueType.ZERO_HEIGHT })
        assertTrue(result.issues.any { it.issueType == ConstraintIssueType.ZERO_WIDTH })
    }
    
    @Test
    fun `validateConstraints should pass for valid constraints`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateConstraints("TestComponent", constraints)
        
        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
    }
    
    @Test
    fun `validateScrollableConstraints should detect critical vertical scrolling issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyColumn",
            constraints,
            ScrollDirection.Vertical
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `validateScrollableConstraints should detect critical horizontal scrolling issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyRow",
            constraints,
            ScrollDirection.Horizontal
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_WIDTH && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `validateScrollableConstraints should pass for safe vertical scrolling`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyColumn",
            constraints,
            ScrollDirection.Vertical
        )
        
        assertTrue(result.isValid)
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }
    
    @Test
    fun `detectAntiPatterns should detect infinite constraint scrollable pattern`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "LazyColumn",
            constraints,
            ScrollDirection.Vertical
        )
        
        assertTrue(antiPatterns.any { 
            it.name == "Infinite Constraint Scrollable" && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `detectAntiPatterns should detect double infinite constraints`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "TestComponent",
            constraints
        )
        
        assertTrue(antiPatterns.any { it.name == "Double Infinite Constraints" })
    }
    
    @Test
    fun `detectAntiPatterns should detect zero constraints pattern`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 0,
            minHeight = 0,
            maxHeight = 0
        )
        
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "TestComponent",
            constraints
        )
        
        assertTrue(antiPatterns.any { it.name == "Zero Constraints" })
    }
    
    @Test
    fun `validatePullToRefreshConstraints should detect critical issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validatePullToRefreshConstraints(
            "PullToRefreshBox",
            constraints
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { it.severity == IssueSeverity.CRITICAL })
        assertTrue(result.recommendations.isNotEmpty())
    }
    
    @Test
    fun `validateLazyListConstraints should detect LazyColumn issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            constraints,
            isVertical = true
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `validateLazyListConstraints should detect LazyRow issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = 1920
        )
        
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyRow",
            constraints,
            isVertical = false
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_WIDTH && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `validateLazyGridConstraints should detect LazyVerticalGrid issue`() {
        val constraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val result = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            constraints,
            isVertical = true
        )
        
        assertFalse(result.isValid)
        assertTrue(result.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }
    
    @Test
    fun `isScrollableSafe should return correct results`() {
        val safeVerticalConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeVerticalConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val safeHorizontalConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeHorizontalConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        
        assertTrue(ConstraintValidator.isScrollableSafe(safeVerticalConstraints, ScrollDirection.Vertical))
        assertFalse(ConstraintValidator.isScrollableSafe(unsafeVerticalConstraints, ScrollDirection.Vertical))
        assertTrue(ConstraintValidator.isScrollableSafe(safeHorizontalConstraints, ScrollDirection.Horizontal))
        assertFalse(ConstraintValidator.isScrollableSafe(unsafeHorizontalConstraints, ScrollDirection.Horizontal))
    }
    
    @Test
    fun `getSafeFallbackConstraints should provide safe fallbacks`() {
        val infiniteConstraints = Constraints(
            minWidth = 0,
            maxWidth = Constraints.Infinity,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        
        val safeConstraints = ConstraintValidator.getSafeFallbackConstraints(
            infiniteConstraints,
            fallbackHeight = 1200,
            fallbackWidth = 1080
        )
        
        assertEquals(1080, safeConstraints.maxWidth)
        assertEquals(1200, safeConstraints.maxHeight)
        assertEquals(0, safeConstraints.minWidth)
        assertEquals(0, safeConstraints.minHeight)
    }
    
    @Test
    fun `getSafeFallbackConstraints should preserve finite constraints`() {
        val partiallyInfiniteConstraints = Constraints(
            minWidth = 100,
            maxWidth = 1080,
            minHeight = 50,
            maxHeight = Constraints.Infinity
        )
        
        val safeConstraints = ConstraintValidator.getSafeFallbackConstraints(
            partiallyInfiniteConstraints,
            fallbackHeight = 1200
        )
        
        assertEquals(1080, safeConstraints.maxWidth) // Preserved
        assertEquals(1200, safeConstraints.maxHeight) // Replaced
        assertEquals(100, safeConstraints.minWidth) // Preserved
        assertEquals(50, safeConstraints.minHeight) // Preserved
    }
}