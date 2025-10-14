package com.devpush.kmp.ui.utils

import androidx.compose.ui.unit.Constraints
import com.devpush.kmp.ui.components.ConstraintIssue
import com.devpush.kmp.ui.components.ScrollDirection
import com.devpush.kmp.ui.components.AntiPattern
import com.devpush.kmp.ui.components.ValidationResult
import com.devpush.kmp.ui.components.IssueSeverity

/**
 * Utility object for logging constraint-related information during development.
 * All logging is automatically disabled in release builds for performance.
 */
object ConstraintLogger {
    
    private const val TAG = "ConstraintLogger"
    private var isLoggingEnabled = true
    
    /**
     * Enable or disable constraint logging
     */
    fun setLoggingEnabled(enabled: Boolean) {
        isLoggingEnabled = enabled
    }
    
    /**
     * Log basic constraint information for a component
     */
    fun logConstraints(
        componentName: String,
        constraints: Constraints
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("=== Constraint Info: $componentName ===")
            appendLine("Min Width: ${constraints.minWidth}")
            appendLine("Max Width: ${if (constraints.maxWidth == Constraints.Infinity) "Infinity" else constraints.maxWidth}")
            appendLine("Min Height: ${constraints.minHeight}")
            appendLine("Max Height: ${if (constraints.maxHeight == Constraints.Infinity) "Infinity" else constraints.maxHeight}")
            appendLine("Has Bounded Width: ${constraints.hasBoundedWidth}")
            appendLine("Has Bounded Height: ${constraints.hasBoundedHeight}")
            appendLine("Has Fixed Width: ${constraints.hasFixedWidth}")
            appendLine("Has Fixed Height: ${constraints.hasFixedHeight}")
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log when infinite constraints are detected
     */
    fun logInfiniteConstraint(
        componentName: String,
        dimension: String,
        fallbackValue: String? = null
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("⚠️ INFINITE CONSTRAINT DETECTED ⚠️")
            appendLine("Component: $componentName")
            appendLine("Dimension: $dimension")
            if (fallbackValue != null) {
                appendLine("Applying fallback: $fallbackValue")
            }
            appendLine("This could cause crashes in scrollable components!")
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log when zero constraints are detected
     */
    fun logZeroConstraint(
        componentName: String,
        dimension: String
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("⚠️ ZERO CONSTRAINT DETECTED ⚠️")
            appendLine("Component: $componentName")
            appendLine("Dimension: $dimension")
            appendLine("This may cause content to not be visible!")
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log validation results from constraint validation
     */
    fun logValidationResult(
        componentName: String,
        result: ValidationResult
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("=== Validation Result: $componentName ===")
            appendLine("Valid: ${result.isValid}")
            appendLine("Issues Found: ${result.issues.size}")
            
            result.issues.forEach { issue ->
                appendLine("  - ${issue.severity}: ${issue.description}")
                appendLine("    Fix: ${issue.suggestedFix}")
            }
            
            if (result.recommendations.isNotEmpty()) {
                appendLine("Recommendations:")
                result.recommendations.forEach { recommendation ->
                    appendLine("  • $recommendation")
                }
            }
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log scrollable-specific validation results
     */
    fun logScrollableValidation(
        componentName: String,
        scrollDirection: ScrollDirection,
        result: ValidationResult
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("=== Scrollable Validation: $componentName ===")
            appendLine("Scroll Direction: $scrollDirection")
            appendLine("Valid: ${result.isValid}")
            
            if (!result.isValid) {
                appendLine("⚠️ SCROLLABLE CONSTRAINT ISSUES DETECTED ⚠️")
                result.issues.forEach { issue ->
                    when (issue.severity) {
                        IssueSeverity.CRITICAL -> appendLine("🔴 CRITICAL: ${issue.description}")
                        IssueSeverity.WARNING -> appendLine("🟡 WARNING: ${issue.description}")
                        IssueSeverity.INFO -> appendLine("🔵 INFO: ${issue.description}")
                    }
                    appendLine("   Fix: ${issue.suggestedFix}")
                }
            } else {
                appendLine("✅ Scrollable constraints are valid")
            }
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log detected anti-patterns
     */
    fun logAntiPattern(
        componentName: String,
        pattern: AntiPattern
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val severityIcon = when (pattern.severity) {
            IssueSeverity.CRITICAL -> "🔴"
            IssueSeverity.WARNING -> "🟡"
            IssueSeverity.INFO -> "🔵"
        }
        
        val message = buildString {
            appendLine("$severityIcon ANTI-PATTERN DETECTED: ${pattern.name}")
            appendLine("Component: $componentName")
            appendLine("Description: ${pattern.description}")
            appendLine("Suggested Fix: ${pattern.suggestedFix}")
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log constraint comparison between parent and child
     */
    fun logConstraintComparison(
        parentName: String,
        parentConstraints: Constraints,
        childName: String,
        childConstraints: Constraints
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("=== Constraint Comparison ===")
            appendLine("Parent: $parentName")
            appendLine("  Max Height: ${if (parentConstraints.maxHeight == Constraints.Infinity) "Infinity" else parentConstraints.maxHeight}")
            appendLine("  Max Width: ${if (parentConstraints.maxWidth == Constraints.Infinity) "Infinity" else parentConstraints.maxWidth}")
            appendLine("Child: $childName")
            appendLine("  Max Height: ${if (childConstraints.maxHeight == Constraints.Infinity) "Infinity" else childConstraints.maxHeight}")
            appendLine("  Max Width: ${if (childConstraints.maxWidth == Constraints.Infinity) "Infinity" else childConstraints.maxWidth}")
            
            // Detect potential issues
            if (parentConstraints.maxHeight == Constraints.Infinity && childConstraints.maxHeight == Constraints.Infinity) {
                appendLine("⚠️ Both parent and child have infinite height constraints!")
            }
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Log performance metrics for constraint validation
     */
    fun logPerformanceMetrics(
        componentName: String,
        validationTimeMs: Long,
        constraintCount: Int
    ) {
        if (!isLoggingEnabled || !isDebugBuild()) return
        
        val message = buildString {
            appendLine("=== Performance Metrics: $componentName ===")
            appendLine("Validation Time: ${validationTimeMs}ms")
            appendLine("Constraints Validated: $constraintCount")
            if (validationTimeMs > 10) {
                appendLine("⚠️ Validation took longer than expected")
            }
        }
        
        println("$TAG: $message")
    }
    
    /**
     * Utility function to check if we're in debug build
     */
    private fun isDebugBuild(): Boolean {
        return BuildConfigHelper.isDebugBuild()
    }
}