package com.devpush.kmp.ui.layout

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.*
import com.devpush.kmp.ui.utils.ConstraintValidator
import kotlin.test.*

/**
 * Tests for edge cases like device rotation and different screen sizes.
 * Ensures layouts remain valid across various device configurations.
 */
class DeviceOrientationConstraintTest {

    // Common device screen dimensions (width x height in dp)
    private val phonePortrait = Constraints(0, 393, 0, 851)      // Pixel 5 portrait
    private val phoneLandscape = Constraints(0, 851, 0, 393)     // Pixel 5 landscape
    private val tabletPortrait = Constraints(0, 820, 0, 1180)    // iPad portrait
    private val tabletLandscape = Constraints(0, 1180, 0, 820)   // iPad landscape
    private val foldableUnfolded = Constraints(0, 673, 0, 841)   // Galaxy Fold unfolded
    private val foldableFolded = Constraints(0, 280, 0, 653)     // Galaxy Fold folded
    private val smallPhone = Constraints(0, 320, 0, 568)         // iPhone SE
    private val largeTablet = Constraints(0, 1024, 0, 1366)      // iPad Pro

    @Test
    fun `CollectionDetailScreen should handle portrait orientation`() {
        // Given - Phone in portrait mode
        val constraints = phonePortrait

        // When - Validating LazyVerticalGrid in portrait
        val result = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            constraints,
            isVertical = true
        )

        // Then - Should be valid in portrait
        assertTrue(result.isValid, "CollectionDetailScreen should work in portrait")
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `CollectionDetailScreen should handle landscape orientation`() {
        // Given - Phone in landscape mode
        val constraints = phoneLandscape

        // When - Validating LazyVerticalGrid in landscape
        val result = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            constraints,
            isVertical = true
        )

        // Then - Should be valid in landscape
        assertTrue(result.isValid, "CollectionDetailScreen should work in landscape")
        assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
    }

    @Test
    fun `GameScreen should handle orientation changes`() {
        val orientations = listOf(
            "Portrait" to phonePortrait,
            "Landscape" to phoneLandscape
        )

        orientations.forEach { (orientation, constraints) ->
            // When - Validating LazyColumn in different orientations
            val result = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )

            // Then - Should be valid in all orientations
            assertTrue(result.isValid, "GameScreen should work in $orientation")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }

    @Test
    fun `StatisticsScreen should handle orientation changes`() {
        val orientations = listOf(
            "Portrait" to phonePortrait,
            "Landscape" to phoneLandscape,
            "Tablet Portrait" to tabletPortrait,
            "Tablet Landscape" to tabletLandscape
        )

        orientations.forEach { (orientation, constraints) ->
            // When - Validating LazyColumn in different orientations
            val result = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )

            // Then - Should be valid in all orientations
            assertTrue(result.isValid, "StatisticsScreen should work in $orientation")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }

    @Test
    fun `layouts should handle tablet screen sizes`() {
        val tabletSizes = listOf(
            "Tablet Portrait" to tabletPortrait,
            "Tablet Landscape" to tabletLandscape,
            "Large Tablet" to largeTablet
        )

        tabletSizes.forEach { (size, constraints) ->
            // Test LazyVerticalGrid for CollectionDetailScreen
            val gridResult = ConstraintValidator.validateLazyGridConstraints(
                "LazyVerticalGrid",
                constraints,
                isVertical = true
            )
            assertTrue(gridResult.isValid, "LazyVerticalGrid should work on $size")

            // Test LazyColumn for other screens
            val listResult = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )
            assertTrue(listResult.isValid, "LazyColumn should work on $size")
        }
    }

    @Test
    fun `layouts should handle foldable device configurations`() {
        val foldableConfigs = listOf(
            "Folded" to foldableFolded,
            "Unfolded" to foldableUnfolded
        )

        foldableConfigs.forEach { (config, constraints) ->
            // Test various scrollable components
            val gridResult = ConstraintValidator.validateLazyGridConstraints(
                "LazyVerticalGrid",
                constraints,
                isVertical = true
            )
            assertTrue(gridResult.isValid, "LazyVerticalGrid should work when $config")

            val listResult = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )
            assertTrue(listResult.isValid, "LazyColumn should work when $config")
        }
    }

    @Test
    fun `layouts should handle small screen devices`() {
        // Given - Small phone constraints (iPhone SE)
        val constraints = smallPhone

        // When - Validating various components on small screen
        val gridResult = ConstraintValidator.validateLazyGridConstraints(
            "LazyVerticalGrid",
            constraints,
            isVertical = true
        )
        val listResult = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            constraints,
            isVertical = true
        )

        // Then - Should work on small screens
        assertTrue(gridResult.isValid, "LazyVerticalGrid should work on small screens")
        assertTrue(listResult.isValid, "LazyColumn should work on small screens")
    }

    @Test
    fun `orientation change should not cause constraint violations`() {
        // Given - Simulating orientation change
        val beforeRotation = phonePortrait
        val afterRotation = phoneLandscape

        // When - Validating constraints before and after rotation
        val beforeResult = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            beforeRotation,
            isVertical = true
        )
        val afterResult = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            afterRotation,
            isVertical = true
        )

        // Then - Both orientations should be valid
        assertTrue(beforeResult.isValid, "Layout should be valid before rotation")
        assertTrue(afterResult.isValid, "Layout should be valid after rotation")
    }

    @Test
    fun `multi-window mode should maintain valid constraints`() {
        // Given - Multi-window constraints (reduced height)
        val multiWindowConstraints = Constraints(0, 393, 0, 400) // Half height

        // When - Validating in multi-window mode
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            multiWindowConstraints,
            isVertical = true
        )

        // Then - Should remain valid in multi-window
        assertTrue(result.isValid, "Layout should work in multi-window mode")
    }

    @Test
    fun `picture-in-picture mode should maintain valid constraints`() {
        // Given - PiP constraints (very small)
        val pipConstraints = Constraints(0, 200, 0, 150)

        // When - Validating in PiP mode
        val result = ConstraintValidator.validateConstraints("PiPContent", pipConstraints)

        // Then - Should remain valid in PiP
        assertTrue(result.isValid, "Layout should work in picture-in-picture mode")
    }

    @Test
    fun `dynamic screen size changes should be handled gracefully`() {
        // Given - Sequence of screen size changes
        val sizeChanges = listOf(
            phonePortrait,
            phoneLandscape,
            tabletPortrait,
            tabletLandscape,
            foldableFolded,
            foldableUnfolded
        )

        // When - Validating each size change
        val results = sizeChanges.map { constraints ->
            ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )
        }

        // Then - All size changes should maintain validity
        results.forEachIndexed { index, result ->
            assertTrue(result.isValid, "Size change $index should maintain valid constraints")
        }
    }

    @Test
    fun `extreme aspect ratios should be handled correctly`() {
        // Given - Extreme aspect ratios
        val extremeWide = Constraints(0, 2000, 0, 400)  // Very wide
        val extremeTall = Constraints(0, 400, 0, 2000)  // Very tall

        // When - Validating extreme aspect ratios
        val wideResult = ConstraintValidator.validateConstraints("WideLayout", extremeWide)
        val tallResult = ConstraintValidator.validateConstraints("TallLayout", extremeTall)

        // Then - Should handle extreme ratios
        assertTrue(wideResult.isValid, "Should handle extremely wide layouts")
        assertTrue(tallResult.isValid, "Should handle extremely tall layouts")
    }

    @Test
    fun `screen density changes should not affect constraint validation`() {
        // Given - Same logical size, different densities (simulated by same constraints)
        val lowDensity = phonePortrait
        val highDensity = phonePortrait // Logical size remains same
        val extraHighDensity = phonePortrait

        val densities = listOf(
            "Low Density" to lowDensity,
            "High Density" to highDensity,
            "Extra High Density" to extraHighDensity
        )

        densities.forEach { (density, constraints) ->
            // When - Validating at different densities
            val result = ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )

            // Then - Should be valid at all densities
            assertTrue(result.isValid, "Layout should work at $density")
        }
    }

    @Test
    fun `accessibility zoom should maintain layout validity`() {
        // Given - Constraints with accessibility zoom (larger text, reduced effective space)
        val zoomedConstraints = Constraints(0, 300, 0, 600) // Reduced effective space

        // When - Validating with zoom
        val result = ConstraintValidator.validateLazyListConstraints(
            "LazyColumn",
            zoomedConstraints,
            isVertical = true
        )

        // Then - Should remain valid with accessibility zoom
        assertTrue(result.isValid, "Layout should work with accessibility zoom")
    }

    @Test
    fun `constraint validation should be consistent across device types`() {
        // Given - Various device types
        val deviceTypes = listOf(
            "Phone Portrait" to phonePortrait,
            "Phone Landscape" to phoneLandscape,
            "Tablet Portrait" to tabletPortrait,
            "Tablet Landscape" to tabletLandscape,
            "Small Phone" to smallPhone,
            "Large Tablet" to largeTablet
        )

        // When - Validating across all device types
        val results = deviceTypes.map { (device, constraints) ->
            device to ConstraintValidator.validateLazyListConstraints(
                "LazyColumn",
                constraints,
                isVertical = true
            )
        }

        // Then - All device types should have consistent validation results
        results.forEach { (device, result) ->
            assertTrue(result.isValid, "$device should have valid constraints")
            assertTrue(result.issues.none { it.severity == IssueSeverity.CRITICAL })
        }
    }
}