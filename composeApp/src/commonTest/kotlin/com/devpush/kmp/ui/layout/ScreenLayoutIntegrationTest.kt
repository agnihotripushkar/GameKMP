package com.devpush.kmp.ui.layout

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.*
import com.devpush.kmp.ui.utils.ConstraintValidator
import com.devpush.features.game.ui.components.validateScrollableConstraints
import kotlin.test.*

/**
 * Integration tests for all screen layouts to ensure proper constraint handling.
 * Tests realistic scenarios that would occur in actual screen implementations.
 */
class ScreenLayoutIntegrationTest {

    @Test
    fun `CollectionDetailScreen layout should handle PullToRefreshBox with LazyVerticalGrid safely`() {
        // Given - The original problematic scenario from CollectionDetailScreen
        // where PullToRefreshBox contained nested Column with LazyVerticalGrid
        val problematicConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity // This was causing the crash
        )

        // When - Validating the problematic scenario
        val problematicResult = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            problematicConstraints,
            isVertical = true
        )

        // Then - Should detect the issue
        assertFalse(problematicResult.isValid)
        assertTrue(problematicResult.issues.any { 
            it.issueType == ConstraintIssueType.INFINITE_HEIGHT && 
            it.severity == IssueSeverity.CRITICAL 
        })

        // Given - The fixed scenario with SafePullToRefreshBox providing finite constraints
        val fixedConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920 // SafePullToRefreshBox ensures finite height
        )

        // When - Validating the fixed scenario
        val fixedResult = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            fixedConstraints,
            isVertical = true
        )

        // Then - Should be valid
        assertTrue(fixedResult.isValid)
        assertTrue(fixedResult.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `GameScreen layout should handle LazyColumn in Scaffold safely`() {
        // Given - GameScreen with LazyColumn in Scaffold structure
        val gameScreenConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920 // Scaffold provides finite constraints
        )

        // When - Validating LazyColumn constraints
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            gameScreenConstraints,
            isVertical = true
        )

        // Then - Should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `StatisticsScreen layout should handle LazyColumn safely`() {
        // Given - StatisticsScreen with LazyColumn for statistics display
        val statisticsScreenConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920 // Scaffold provides finite constraints
        )

        // When - Validating LazyColumn constraints
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            statisticsScreenConstraints,
            isVertical = true
        )

        // Then - Should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `GameDetailsScreen layout should handle scrollable content safely`() {
        // Given - GameDetailsScreen with potential scrollable content
        val gameDetailsConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = 1920
        )

        // When - Validating scrollable constraints
        val result = ConstraintValidator.validateScrollableConstraints(
            "LazyColumn",
            gameDetailsConstraints,
            ScrollDirection.Vertical
        )

        // Then - Should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `nested scrollable components should be detected and flagged`() {
        // Given - Scenario with nested scrollable components (anti-pattern)
        val nestedScrollableConstraints = Constraints(
            minWidth = 0,
            maxWidth = 1080,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )

        // When - Detecting anti-patterns
        val antiPatterns = ConstraintValidator.detectAntiPatterns(
            "LazyColumn",
            nestedScrollableConstraints,
            ScrollDirection.Vertical
        )

        // Then - Should detect nested scrollable anti-pattern
        assertTrue(antiPatterns.any { 
            it.name == "Infinite Constraint Scrollable" && 
            it.severity == IssueSeverity.CRITICAL 
        })
    }

    @Test
    fun `multiple screen layouts should work together without conflicts`() {
        // Given - Multiple screens with different constraint scenarios
        val screenConstraints = listOf(
            // CollectionDetailScreen (fixed)
            Constraints(0, 1080, 0, 1920),
            // GameScreen
            Constraints(0, 1080, 0, 1920),
            // StatisticsScreen
            Constraints(0, 1080, 0, 1920),
            // GameDetailsScreen
            Constraints(0, 1080, 0, 1920)
        )

        val screenNames = listOf(
            "CollectionDetailScreen",
            "GameScreen", 
            "StatisticsScreen",
            "GameDetailsScreen"
        )

        // When - Validating all screen constraints
        val results = screenConstraints.mapIndexed { index, constraints ->
            ConstraintValidator.validateConstraints(screenNames[index], constraints)
        }

        // Then - All screens should have valid constraints
        results.forEach { result ->
            assertTrue(result.isValid, "Screen should have valid constraints")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }

    @Test
    fun `screen transitions should maintain constraint validity`() {
        // Given - Constraints during screen transitions
        val transitionConstraints = listOf(
            // Initial screen
            Constraints(0, 1080, 0, 1920),
            // During transition (potentially different)
            Constraints(0, 1080, 0, 1800),
            // Final screen
            Constraints(0, 1080, 0, 1920)
        )

        // When - Validating constraints during transitions
        val results = transitionConstraints.map { constraints ->
            ConstraintValidator.validateScrollableConstraints(
                "LazyColumn",
                constraints,
                ScrollDirection.Vertical
            )
        }

        // Then - All transition states should be valid
        results.forEach { result ->
            assertTrue(result.isValid, "Transition state should maintain valid constraints")
        }
    }

    @Test
    fun `complex layout hierarchies should be validated correctly`() {
        // Given - Complex nested layout scenario
        val complexLayoutConstraints = Constraints(0, 1080, 0, 1920)

        // When - Validating multiple nested components
        val scaffoldResult = ConstraintValidator.validateConstraints("Scaffold", complexLayoutConstraints)
        val columnResult = ConstraintValidator.validateConstraints("Column", complexLayoutConstraints)
        val lazyColumnResult = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn", 
            complexLayoutConstraints, 
            isVertical = true
        )

        // Then - All components in hierarchy should be valid
        assertTrue(scaffoldResult.isValid)
        assertTrue(columnResult.isValid)
        assertTrue(lazyColumnResult.isValid)
    }

    @Test
    fun `error recovery scenarios should provide valid fallbacks`() {
        // Given - Problematic constraints that need recovery
        val problematicConstraints = Constraints(0, Constraints.Infinity, 0, Constraints.Infinity)

        // When - Getting safe fallback constraints
        val safeConstraints = ConstraintValidator.getSafeFallbackConstraints(
            problematicConstraints,
            fallbackHeight = 1920,
            fallbackWidth = 1080
        )

        // Then - Fallback should be valid
        val fallbackResult = ConstraintValidator.validateConstraints("FallbackComponent", safeConstraints)
        assertTrue(fallbackResult.isValid)
        assertEquals(1080, safeConstraints.maxWidth)
        assertEquals(1920, safeConstraints.maxHeight)
    }

    @Test
    fun `screen layout validation should handle loading states`() {
        // Given - Loading state constraints (potentially different sizing)
        val loadingConstraints = Constraints(0, 1080, 0, 200) // Smaller height during loading

        // When - Validating loading state layout
        val result = ConstraintValidator.validateConstraints("LoadingIndicator", loadingConstraints)

        // Then - Loading state should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun `screen layout validation should handle empty states`() {
        // Given - Empty state constraints
        val emptyStateConstraints = Constraints(0, 1080, 0, 400) // Moderate height for empty state

        // When - Validating empty state layout
        val result = ConstraintValidator.validateConstraints("EmptyState", emptyStateConstraints)

        // Then - Empty state should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun `screen layout validation should handle error states`() {
        // Given - Error state constraints
        val errorStateConstraints = Constraints(0, 1080, 0, 300) // Smaller height for error display

        // When - Validating error state layout
        val result = ConstraintValidator.validateConstraints("ErrorState", errorStateConstraints)

        // Then - Error state should be valid
        assertTrue(result.isValid)
        assertTrue(result.issues.isEmpty())
    }

    @Test
    fun `integration test should validate complete user flow constraints`() {
        // Given - User flow through multiple screens
        val userFlowConstraints = listOf(
            // 1. GameScreen (browsing games)
            Constraints(0, 1080, 0, 1920),
            // 2. GameDetailsScreen (viewing game details)
            Constraints(0, 1080, 0, 1920),
            // 3. CollectionDetailScreen (managing collections)
            Constraints(0, 1080, 0, 1920),
            // 4. StatisticsScreen (viewing statistics)
            Constraints(0, 1080, 0, 1920)
        )

        val flowSteps = listOf(
            "GameScreen",
            "GameDetailsScreen",
            "CollectionDetailScreen", 
            "StatisticsScreen"
        )

        // When - Validating entire user flow
        val flowResults = userFlowConstraints.mapIndexed { index, constraints ->
            ConstraintValidator.validateConstraints(flowSteps[index], constraints)
        }

        // Then - Entire flow should maintain valid constraints
        flowResults.forEachIndexed { index, result ->
            assertTrue(result.isValid, "${flowSteps[index]} should have valid constraints")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }
}