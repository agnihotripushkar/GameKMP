package com.devpush.features.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Default styling functions for expressive Material 3 components.
 * These functions provide enhanced styling with expressive design tokens.
 */
object ExpressiveDefaults {
    
    /**
     * Creates expressive card colors with enhanced contrast and dynamic theming support.
     */
    @Composable
    fun expressiveCardColors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): CardColors = CardDefaults.cardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive card elevation with enhanced shadow depth.
     */
    @Composable
    fun expressiveCardElevation(
        defaultElevation: Dp = ExpressiveTokens.Elevation.Level2,
        pressedElevation: Dp = ExpressiveTokens.Elevation.Level1,
        focusedElevation: Dp = ExpressiveTokens.Elevation.Level3,
        hoveredElevation: Dp = ExpressiveTokens.Elevation.Level3,
        draggedElevation: Dp = ExpressiveTokens.Elevation.Level4,
        disabledElevation: Dp = ExpressiveTokens.Elevation.Level0
    ): CardElevation = CardDefaults.cardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation
    )
    
    /**
     * Creates expressive filled button colors with enhanced state layer effects.
     */
    @Composable
    fun expressiveFilledButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive outlined button colors with enhanced border and state effects.
     */
    @Composable
    fun expressiveOutlinedButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.outlinedButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive text button colors with enhanced state layer effects.
     */
    @Composable
    fun expressiveTextButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ButtonDefaults.textButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive button elevation with enhanced shadow depth.
     */
    @Composable
    fun expressiveButtonElevation(
        defaultElevation: Dp = ExpressiveTokens.Elevation.Level1,
        pressedElevation: Dp = ExpressiveTokens.Elevation.Level2,
        focusedElevation: Dp = ExpressiveTokens.Elevation.Level2,
        hoveredElevation: Dp = ExpressiveTokens.Elevation.Level2,
        disabledElevation: Dp = ExpressiveTokens.Elevation.Level0
    ): ButtonElevation = ButtonDefaults.buttonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation
    )
    
    /**
     * Creates expressive outlined text field colors with enhanced focus indicators.
     */
    @Composable
    fun expressiveOutlinedTextFieldColors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTextColor: Color = MaterialTheme.colorScheme.error,
        focusedContainerColor: Color = Color.Transparent,
        unfocusedContainerColor: Color = Color.Transparent,
        disabledContainerColor: Color = Color.Transparent,
        errorContainerColor: Color = Color.Transparent,
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        errorCursorColor: Color = MaterialTheme.colorScheme.error,
        focusedBorderColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor: Color = MaterialTheme.colorScheme.outline,
        disabledBorderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        errorBorderColor: Color = MaterialTheme.colorScheme.error,
        focusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTrailingIconColor: Color = MaterialTheme.colorScheme.error,
        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorLabelColor: Color = MaterialTheme.colorScheme.error,
        focusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedSupportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSupportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSupportingTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        focusedPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPrefixColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSuffixColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        disabledTextColor = disabledTextColor,
        errorTextColor = errorTextColor,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        cursorColor = cursorColor,
        errorCursorColor = errorCursorColor,
        focusedBorderColor = focusedBorderColor,
        unfocusedBorderColor = unfocusedBorderColor,
        disabledBorderColor = disabledBorderColor,
        errorBorderColor = errorBorderColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        unfocusedTrailingIconColor = unfocusedTrailingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
        errorTrailingIconColor = errorTrailingIconColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        focusedPlaceholderColor = focusedPlaceholderColor,
        unfocusedPlaceholderColor = unfocusedPlaceholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor,
        errorPlaceholderColor = errorPlaceholderColor,
        focusedSupportingTextColor = focusedSupportingTextColor,
        unfocusedSupportingTextColor = unfocusedSupportingTextColor,
        disabledSupportingTextColor = disabledSupportingTextColor,
        errorSupportingTextColor = errorSupportingTextColor,
        focusedPrefixColor = focusedPrefixColor,
        unfocusedPrefixColor = unfocusedPrefixColor,
        disabledPrefixColor = disabledPrefixColor,
        errorPrefixColor = errorPrefixColor,
        focusedSuffixColor = focusedSuffixColor,
        unfocusedSuffixColor = unfocusedSuffixColor,
        disabledSuffixColor = disabledSuffixColor,
        errorSuffixColor = errorSuffixColor
    )
    
    /**
     * Creates expressive filled text field colors with enhanced container styling.
     */
    @Composable
    fun expressiveFilledTextFieldColors(
        focusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTextColor: Color = MaterialTheme.colorScheme.error,
        focusedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        unfocusedContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        errorContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
        cursorColor: Color = MaterialTheme.colorScheme.primary,
        errorCursorColor: Color = MaterialTheme.colorScheme.error,
        focusedIndicatorColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledIndicatorColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorIndicatorColor: Color = MaterialTheme.colorScheme.error,
        focusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorLeadingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedTrailingIconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorTrailingIconColor: Color = MaterialTheme.colorScheme.error,
        focusedLabelColor: Color = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorLabelColor: Color = MaterialTheme.colorScheme.error,
        focusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorPlaceholderColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedSupportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSupportingTextColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSupportingTextColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorSupportingTextColor: Color = MaterialTheme.colorScheme.error,
        focusedPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPrefixColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorPrefixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledSuffixColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        errorSuffixColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
    ): TextFieldColors = TextFieldDefaults.colors(
        focusedTextColor = focusedTextColor,
        unfocusedTextColor = unfocusedTextColor,
        disabledTextColor = disabledTextColor,
        errorTextColor = errorTextColor,
        focusedContainerColor = focusedContainerColor,
        unfocusedContainerColor = unfocusedContainerColor,
        disabledContainerColor = disabledContainerColor,
        errorContainerColor = errorContainerColor,
        cursorColor = cursorColor,
        errorCursorColor = errorCursorColor,
        focusedIndicatorColor = focusedIndicatorColor,
        unfocusedIndicatorColor = unfocusedIndicatorColor,
        disabledIndicatorColor = disabledIndicatorColor,
        errorIndicatorColor = errorIndicatorColor,
        focusedLeadingIconColor = focusedLeadingIconColor,
        unfocusedLeadingIconColor = unfocusedLeadingIconColor,
        disabledLeadingIconColor = disabledLeadingIconColor,
        errorLeadingIconColor = errorLeadingIconColor,
        focusedTrailingIconColor = focusedTrailingIconColor,
        unfocusedTrailingIconColor = unfocusedTrailingIconColor,
        disabledTrailingIconColor = disabledTrailingIconColor,
        errorTrailingIconColor = errorTrailingIconColor,
        focusedLabelColor = focusedLabelColor,
        unfocusedLabelColor = unfocusedLabelColor,
        disabledLabelColor = disabledLabelColor,
        errorLabelColor = errorLabelColor,
        focusedPlaceholderColor = focusedPlaceholderColor,
        unfocusedPlaceholderColor = unfocusedPlaceholderColor,
        disabledPlaceholderColor = disabledPlaceholderColor,
        errorPlaceholderColor = errorPlaceholderColor,
        focusedSupportingTextColor = focusedSupportingTextColor,
        unfocusedSupportingTextColor = unfocusedSupportingTextColor,
        disabledSupportingTextColor = disabledSupportingTextColor,
        errorSupportingTextColor = errorSupportingTextColor,
        focusedPrefixColor = focusedPrefixColor,
        unfocusedPrefixColor = unfocusedPrefixColor,
        disabledPrefixColor = disabledPrefixColor,
        errorPrefixColor = errorPrefixColor,
        focusedSuffixColor = focusedSuffixColor,
        unfocusedSuffixColor = unfocusedSuffixColor,
        disabledSuffixColor = disabledSuffixColor,
        errorSuffixColor = errorSuffixColor
    )
    
    /**
     * Creates expressive shapes using design tokens.
     */
    object Shapes {
        val Card: Shape = ExpressiveTokens.Shapes.Card
        val CardSmall: Shape = ExpressiveTokens.Shapes.CardSmall
        val Button: Shape = ExpressiveTokens.Shapes.Button
        val TextField: Shape = ExpressiveTokens.Shapes.TextField
        val FAB: Shape = ExpressiveTokens.Shapes.FAB
        
        /**
         * Creates a custom expressive shape with specified corner radius.
         */
        fun custom(cornerRadius: Dp): Shape = RoundedCornerShape(cornerRadius)
    }
    
    /**
     * Creates expressive border stroke for outlined components.
     */
    @Composable
    fun expressiveBorderStroke(
        width: Dp = 1.dp,
        color: Color = MaterialTheme.colorScheme.outline
    ): BorderStroke = BorderStroke(width, color)
}