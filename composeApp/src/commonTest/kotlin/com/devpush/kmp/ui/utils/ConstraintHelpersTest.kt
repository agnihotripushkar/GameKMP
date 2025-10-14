package com.devpush.kmp.ui.utils

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.ScrollDirection
import kotlin.test.*

class ConstraintHelpersTest {
    
    @Test
    fun `areConstraintsSafeForScrolling should return correct results for vertical scrolling`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        
        assertTrue(ConstraintHelpers.areConstraintsSafeForScrolling(safeConstraints, ScrollDirection.Vertical))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeConstraints, ScrollDirection.Vertical))
    }
    
    @Test
    fun `areConstraintsSafeForScrolling should return correct results for horizontal scrolling`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        
        assertTrue(ConstraintHelpers.areConstraintsSafeForScrolling(safeConstraints, ScrollDirection.Horizontal))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeConstraints, ScrollDirection.Horizontal))
    }
    
    @Test
    fun `areConstraintsSafeForScrolling should return correct results for both directions`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeHeightConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val unsafeWidthConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val unsafeBothConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        
        assertTrue(ConstraintHelpers.areConstraintsSafeForScrolling(safeConstraints, ScrollDirection.Both))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeHeightConstraints, ScrollDirection.Both))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeWidthConstraints, ScrollDirection.Both))
        assertFalse(ConstraintHelpers.areConstraintsSafeForScrolling(unsafeBothConstraints, ScrollDirection.Both))
    }
    
    @Test
    fun `getConstraintDescription should format constraints correctly`() {
        val constraints = Constraints(10, 1080, 20, 1920)
        val description = ConstraintHelpers.getConstraintDescription(constraints)
        
        assertEquals("Constraints(minWidth=10, maxWidth=1080, minHeight=20, maxHeight=1920)", description)
    }
    
    @Test
    fun `getConstraintDescription should handle infinite constraints`() {
        val constraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        val description = ConstraintHelpers.getConstraintDescription(constraints)
        
        assertEquals("Constraints(minWidth=0, maxWidth=∞, minHeight=0, maxHeight=∞)", description)
    }
    
    @Test
    fun `willCauseScrollableCrash should detect crash scenarios`() {
        val verticalCrashConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val horizontalCrashConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        
        assertTrue(ConstraintHelpers.willCauseScrollableCrash(verticalCrashConstraints, ScrollDirection.Vertical))
        assertTrue(ConstraintHelpers.willCauseScrollableCrash(horizontalCrashConstraints, ScrollDirection.Horizontal))
        assertFalse(ConstraintHelpers.willCauseScrollableCrash(safeConstraints, ScrollDirection.Vertical))
        assertFalse(ConstraintHelpers.willCauseScrollableCrash(safeConstraints, ScrollDirection.Horizontal))
    }
    
    @Test
    fun `getSuggestedFix should provide appropriate fixes for scrollable crashes`() {
        val verticalCrashConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        
        val fix = ConstraintHelpers.getSuggestedFix(
            verticalCrashConstraints,
            "LazyColumn",
            ScrollDirection.Vertical
        )
        
        assertTrue(fix.contains("ConstrainedScrollableContainer"))
        assertTrue(fix.contains("weight modifier"))
    }
    
    @Test
    fun `getSuggestedFix should provide appropriate fixes for double infinite constraints`() {
        val doubleInfiniteConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)
        
        val fix = ConstraintHelpers.getSuggestedFix(
            doubleInfiniteConstraints,
            "TestComponent"
        )
        
        assertTrue(fix.contains("finite dimension"))
        assertTrue(fix.contains("BoxWithConstraints"))
    }
    
    @Test
    fun `getSuggestedFix should provide appropriate fixes for zero constraints`() {
        val zeroConstraints = Constraints(0, 0, 0, 0)
        
        val fix = ConstraintHelpers.getSuggestedFix(
            zeroConstraints,
            "TestComponent"
        )
        
        assertTrue(fix.contains("non-zero constraints"))
    }
    
    @Test
    fun `createValidationSummary should create correct summary for safe constraints`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        
        val summary = ConstraintHelpers.createValidationSummary(
            "TestComponent",
            safeConstraints
        )
        
        assertTrue(summary.contains("✅"))
        assertTrue(summary.contains("safe"))
    }
    
    @Test
    fun `createValidationSummary should create correct summary for problematic constraints`() {
        val problematicConstraints = Constraints(0, Constraints.Infinity, 0, 0)
        
        val summary = ConstraintHelpers.createValidationSummary(
            "TestComponent",
            problematicConstraints
        )
        
        assertTrue(summary.contains("⚠️"))
        assertTrue(summary.contains("Issues found"))
        assertTrue(summary.contains("Infinite width constraint"))
        assertTrue(summary.contains("Zero height constraint"))
    }
    
    @Test
    fun `createValidationSummary should detect scrollable crash scenarios`() {
        val crashConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        
        val summary = ConstraintHelpers.createValidationSummary(
            "LazyColumn",
            crashConstraints,
            ScrollDirection.Vertical
        )
        
        assertTrue(summary.contains("⚠️"))
        assertTrue(summary.contains("scrollable component crash"))
    }
}

class ConstraintExtensionsTest {
    
    @Test
    fun `isSafeForVerticalScrolling should work correctly`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        
        assertTrue(safeConstraints.isSafeForVerticalScrolling())
        assertFalse(unsafeConstraints.isSafeForVerticalScrolling())
    }
    
    @Test
    fun `isSafeForHorizontalScrolling should work correctly`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val unsafeConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        
        assertTrue(safeConstraints.isSafeForHorizontalScrolling())
        assertFalse(unsafeConstraints.isSafeForHorizontalScrolling())
    }
    
    @Test
    fun `hasInfiniteHeight should work correctly`() {
        val infiniteHeightConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val finiteHeightConstraints = Constraints(0, 1080, 0, 1920)
        
        assertTrue(infiniteHeightConstraints.hasInfiniteHeight())
        assertFalse(finiteHeightConstraints.hasInfiniteHeight())
    }
    
    @Test
    fun `hasInfiniteWidth should work correctly`() {
        val infiniteWidthConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        val finiteWidthConstraints = Constraints(0, 1080, 0, 1920)
        
        assertTrue(infiniteWidthConstraints.hasInfiniteWidth())
        assertFalse(finiteWidthConstraints.hasInfiniteWidth())
    }
    
    @Test
    fun `hasZeroHeight should work correctly`() {
        val zeroHeightConstraints = Constraints(0, 1080, 0, 0)
        val nonZeroHeightConstraints = Constraints(0, 1080, 0, 1920)
        
        assertTrue(zeroHeightConstraints.hasZeroHeight())
        assertFalse(nonZeroHeightConstraints.hasZeroHeight())
    }
    
    @Test
    fun `hasZeroWidth should work correctly`() {
        val zeroWidthConstraints = Constraints(0, 0, 0, 1920)
        val nonZeroWidthConstraints = Constraints(0, 1080, 0, 1920)
        
        assertTrue(zeroWidthConstraints.hasZeroWidth())
        assertFalse(nonZeroWidthConstraints.hasZeroWidth())
    }
    
    @Test
    fun `isFullyBounded should work correctly`() {
        val boundedConstraints = Constraints(0, 1080, 0, 1920)
        val unboundedHeightConstraints = Constraints(0, 1080, 0, Constraints.Infinity)
        val unboundedWidthConstraints = Constraints(0, Constraints.Infinity, 0, 1920)
        
        assertTrue(boundedConstraints.isFullyBounded())
        assertFalse(unboundedHeightConstraints.isFullyBounded())
        assertFalse(unboundedWidthConstraints.isFullyBounded())
    }
    
    @Test
    fun `getProblematicDimensions should identify all problems`() {
        val problematicConstraints = Constraints(0, Constraints.Infinity, 0, 0)
        val problems = problematicConstraints.getProblematicDimensions()
        
        assertEquals(2, problems.size)
        assertTrue(problems.contains("infinite width"))
        assertTrue(problems.contains("zero height"))
    }
    
    @Test
    fun `getProblematicDimensions should return empty list for safe constraints`() {
        val safeConstraints = Constraints(0, 1080, 0, 1920)
        val problems = safeConstraints.getProblematicDimensions()
        
        assertTrue(problems.isEmpty())
    }
}