package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
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
 * Expressive Card component with enhanced state layer animations and Material 3 styling.
 * Provides smooth hover, press, and focus state animations with spring physics.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    shape: Shape = ExpressiveTokens.Shapes.Card,
    colors: CardColors = ExpressiveDefaults.expressiveCardColors(),
    elevation: CardElevation = ExpressiveDefaults.expressiveCardElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Enhanced state layer animations
    val animatedElevation by ExpressiveAnimations.animatedElevation(
        interactionSource = interactionSource,
        defaultElevation = ExpressiveTokens.Elevation.Level2,
        pressedElevation = ExpressiveTokens.Elevation.Level1,
        hoveredElevation = ExpressiveTokens.Elevation.Level3,
        focusedElevation = ExpressiveTokens.Elevation.Level3,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Smooth press scale animation
    val animatedScale by ExpressiveAnimations.animatedPressScale(
        interactionSource = interactionSource,
        defaultScale = 1f,
        pressedScale = 0.98f,
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
    
    if (onClick != null) {
        Card(
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
            elevation = CardDefaults.cardElevation(
                defaultElevation = animatedElevation,
                pressedElevation = ExpressiveTokens.Elevation.Level1,
                focusedElevation = ExpressiveTokens.Elevation.Level3,
                hoveredElevation = ExpressiveTokens.Elevation.Level3,
                draggedElevation = ExpressiveTokens.Elevation.Level4,
                disabledElevation = ExpressiveTokens.Elevation.Level0
            ),
            border = border,
            interactionSource = interactionSource,
            content = content
        )
    } else {
        Card(
            modifier = modifier
                .let { mod ->
                    contentDescription?.let { desc ->
                        mod.semantics { this.contentDescription = desc }
                    } ?: mod
                },
            shape = shape,
            colors = colors,
            elevation = CardDefaults.cardElevation(
                defaultElevation = ExpressiveTokens.Elevation.Level2,
                pressedElevation = ExpressiveTokens.Elevation.Level1,
                focusedElevation = ExpressiveTokens.Elevation.Level3,
                hoveredElevation = ExpressiveTokens.Elevation.Level3,
                draggedElevation = ExpressiveTokens.Elevation.Level4,
                disabledElevation = ExpressiveTokens.Elevation.Level0
            ),
            border = border,
            content = content
        )
    }
}

/**
 * Expressive Card component without click functionality but with enhanced visual states.
 */
@Composable
fun ExpressiveCard(
    modifier: Modifier = Modifier,
    shape: Shape = ExpressiveTokens.Shapes.Card,
    colors: CardColors = ExpressiveDefaults.expressiveCardColors(),
    elevation: CardElevation = ExpressiveDefaults.expressiveCardElevation(),
    border: BorderStroke? = null,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.let { mod ->
            contentDescription?.let { desc ->
                mod.semantics { this.contentDescription = desc }
            } ?: mod
        },
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        content = content
    )
}

/**
 * Default values for ExpressiveCard components.
 */
object ExpressiveCardDefaults {
    
    /**
     * Creates expressive card colors with enhanced theming.
     */
    @Composable
    fun colors(
        containerColor: Color = MaterialTheme.colorScheme.surfaceContainer,
        contentColor: Color = MaterialTheme.colorScheme.onSurface,
        disabledContainerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.38f),
        disabledContentColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    ): CardColors = ExpressiveDefaults.expressiveCardColors(
        containerColor = containerColor,
        contentColor = contentColor,
        disabledContainerColor = disabledContainerColor,
        disabledContentColor = disabledContentColor
    )
    
    /**
     * Creates expressive card elevation with enhanced depth.
     */
    @Composable
    fun elevation(
        defaultElevation: Dp = ExpressiveTokens.Elevation.Level2,
        pressedElevation: Dp = ExpressiveTokens.Elevation.Level1,
        focusedElevation: Dp = ExpressiveTokens.Elevation.Level3,
        hoveredElevation: Dp = ExpressiveTokens.Elevation.Level3,
        draggedElevation: Dp = ExpressiveTokens.Elevation.Level4,
        disabledElevation: Dp = ExpressiveTokens.Elevation.Level0
    ): CardElevation = ExpressiveDefaults.expressiveCardElevation(
        defaultElevation = defaultElevation,
        pressedElevation = pressedElevation,
        focusedElevation = focusedElevation,
        hoveredElevation = hoveredElevation,
        draggedElevation = draggedElevation,
        disabledElevation = disabledElevation
    )
    
    /**
     * Creates expressive border stroke for outlined cards.
     */
    @Composable
    fun outlinedBorder(
        width: Dp = ExpressiveTokens.Elevation.Level1,
        color: Color = MaterialTheme.colorScheme.outline
    ): BorderStroke = ExpressiveDefaults.expressiveBorderStroke(width, color)
}