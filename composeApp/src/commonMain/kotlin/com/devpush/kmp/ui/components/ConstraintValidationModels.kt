package com.devpush.kmp.ui.components

/**
 * Represents the result of constraint validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val issues: List<ConstraintIssue>,
    val recommendations: List<String>
)

/**
 * Represents a constraint issue found during validation
 */
data class ConstraintIssue(
    val componentName: String,
    val issueType: ConstraintIssueType,
    val description: String,
    val suggestedFix: String,
    val severity: IssueSeverity
)

/**
 * Types of constraint issues that can be detected
 */
enum class ConstraintIssueType {
    INFINITE_HEIGHT,
    INFINITE_WIDTH,
    NESTED_SCROLLABLE,
    UNBOUNDED_CONSTRAINT
}

/**
 * Severity levels for constraint issues
 */
enum class IssueSeverity {
    CRITICAL, // Will cause crash
    WARNING,  // May cause issues
    INFO      // Best practice violation
}

/**
 * Scroll direction for scrollable components
 */
enum class ScrollDirection {
    VERTICAL,
    HORIZONTAL,
    BOTH
}

/**
 * Represents an anti-pattern in layout design
 */
data class AntiPattern(
    val name: String,
    val description: String,
    val suggestedFix: String,
    val severity: IssueSeverity
)