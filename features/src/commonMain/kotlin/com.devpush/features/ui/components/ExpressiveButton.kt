package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import com.devpush.features.ui.theme.ExpressiveAnimations
import com.devpush.features.ui.theme.ExpressiveDefaults
import com.devpush.features.ui.theme.ExpressiveTokens

/**
 * Expressive Filled Button with enhanced state layer animations and Material 3 styling.
 * Provides smooth hover, press, and focus state animations with spring physics.
 */
@Composable
fun ExpressiveFilledButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveTokens.Shapes.Button,
    colors: ButtonColors = ExpressiveDefaults.expressiveFilledButtonColors(),
    elevation: ButtonElevation? = ExpressiveDefaults.expressiveButtonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    // Enhanced state layer animations
    val animatedElevation by ExpressiveAnimations.animatedElevation(
        interactionSource = interactionSource,
        defaultElevation = ExpressiveTokens.Elevation.Level1,
        pressedElevation = ExpressiveTokens.Elevation.Level2,
        hoveredElevation = ExpressiveTokens.Elevation.Level2,
        focusedElevation = ExpressiveTokens.Elevation.Level2,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Smooth press scale animation
    val animatedScale by ExpressiveAnimations.animatedPressScale(
        interactionSource = interactionSource,
        defaultScale = 1f,
        pressedScale = 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    // State layer color animation
    val stateLayerColor by ExpressiveAnimations.animatedStateLayerColor(
        interactionSource = interactionSource,
        defaultColor = Color.Transparent,
        pressedColor = colors.contentColor.copy(alpha = 0.12f),
        hoveredColor = colors.contentColor.copy(alpha = 0.08f),
        focusedColor = colors.contentColor.copy(alpha = 0.12f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    Button(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .let { mod ->
                contentDescription?.let { desc ->
                    mod.semantics { this.contentDescription = desc }
                } ?: mod
            },
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = animatedElevation,
            pressedElevation = ExpressiveTokens.Elevation.Level2,
            focusedElevation = ExpressiveTokens.Elevation.Level2,
            hoveredElevation = ExpressiveTokens.Elevation.Level2,
            disabledElevation = ExpressiveTokens.Elevation.Level0
        ),
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive Outlined Button with enhanced state layer animations and Material 3 styling.
 */
@Composable
fun ExpressiveOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveTokens.Shapes.Button,
    colors: ButtonColors = ExpressiveDefaults.expressiveOutlinedButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = ExpressiveButtonDefaults.outlinedBorder(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    // Smooth press scale animation
    val animatedScale by ExpressiveAnimations.animatedPressScale(
        interactionSource = interactionSource,
        defaultScale = 1f,
        pressedScale = 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    // State layer color animation
    val stateLayerColor by ExpressiveAnimations.animatedStateLayerColor(
        interactionSource = interactionSource,
        defaultColor = Color.Transparent,
        pressedColor = colors.contentColor.copy(alpha = 0.12f),
        hoveredColor = colors.contentColor.copy(alpha = 0.08f),
        focusedColor = colors.contentColor.copy(alpha = 0.12f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .let { mod ->
                contentDescription?.let { desc ->
                    mod.semantics { this.contentDescription = desc }
                } ?: mod
            },
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive Text Button with enhanced state layer animations and Material 3 styling.
 */
@Composable
fun ExpressiveTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ExpressiveTokens.Shapes.Button,
    colors: ButtonColors = ExpressiveDefaults.expressiveTextButtonColors(),
    elevation: ButtonElevation? = null,
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    content: @Composable RowScope.() -> Unit
) {
    // Smooth press scale animation
    val animatedScale by ExpressiveAnimations.animatedPressScale(
        interactionSource = interactionSource,
        defaultScale = 1f,
        pressedScale = 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    )
    
    // State layer color animation
    val stateLayerColor by ExpressiveAnimations.animatedStateLayerColor(
        interactionSource = interactionSource,
        defaultColor = Color.Transparent,
        pressedColor = colors.contentColor.copy(alpha = 0.12f),
        hoveredColor = colors.contentColor.copy(alpha = 0.08f),
        focusedColor = colors.contentColor.copy(alpha = 0.12f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    TextButton(
        onClick = onClick,
        modifier = modifier
            .scale(animatedScale)
            .let { mod ->
                contentDescription?.let { desc ->
                    mod.semantics { this.contentDescription = desc }
                } ?: mod
            },
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Default values for ExpressiveButton components.
 */
object ExpressiveButtonDefaults {
    
    /**
     * Creates expressive filled button colors.
     */
    @Composable
    fun filledButtonColors(
        containerColor: Color = MaterialTheme.colorScheme.primary,
        contentColor: Color = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ExpressiveDefaults.expressiveFilledButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive outlined button colors.
     */
    @Composable
    fun outlinedButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ExpressiveDefaults.expressiveOutlinedButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive text button colors.
     */
    @Composable
    fun textButtonColors(
        containerColor: Color = Color.Transparent,
        contentColor: Color = MaterialTheme.colorScheme.primary,
        disabledContainerColor: Color = Color.Transparent,
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): ButtonColors = ExpressiveDefaults.expressiveTextButtonColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive button elevation.
     */
    @Composable
    fun elevation(
        defaultElevation: Dp = ExpressiveTokens.Elevation.Level1,
        pressedElevation: Dp = ExpressiveTokens.Elevation.Level2,
        focusedElevation: Dp = ExpressiveTokens.Elevation.Level2,
        hoveredElevation: Dp = ExpressiveTokens.Elevation.Level2,
        disabledElevation: Dp = ExpressiveTokens.Elevation.Level0
    ): ButtonElevation = ExpressiveDefaults.expressiveButtonElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        disabledElevation = disabledElevation
    )
    
    /**
     * Creates expressive border stroke for outlined buttons.
     */
    @Composable
    fun outlinedBorder(
        width: Dp = ExpressiveTokens.Elevation.Level1,
        color: Color = MaterialTheme.colorScheme.outline
    ): BorderStroke = ExpressiveDefaults.expressiveBorderStroke(width, color)
}