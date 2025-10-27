@file:OptIn(ExperimentalMaterial3Api::class)

package com.devpush.features.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devpush.features.ui.theme.ExpressiveMotion
import com.devpush.features.ui.theme.ExpressiveTokens

/**
 * Expressive Dialog with enhanced entrance/exit animations and Material 3 styling.
 * Provides smooth motion choreography for dialog presentation and dismissal.
 */
@Composable
fun ExpressiveDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = RoundedCornerShape(ExpressiveTokens.CornerRadius.Large),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3,
    shadowElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Enhanced entrance animation with spring physics
    val entranceProgress by ExpressiveMotion.entranceAnimation(
        visible = true,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties
    ) {
        Surface(
            modifier = modifier
                .scale(0.8f + (0.2f * entranceProgress))
                .alpha(entranceProgress)
                .let { mod ->
                    contentDescription?.let { desc ->
                        mod.semantics { this.contentDescription = desc }
                    } ?: mod
                },
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = tonalElevation,
            shadowElevation = shadowElevation
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                content = content
            )
        }
    }
}

/**
 * Expressive Alert Dialog with enhanced animations and Material 3 styling.
 */
@Composable
fun ExpressiveAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = RoundedCornerShape(ExpressiveTokens.CornerRadius.Large),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    iconContentColor: Color = MaterialTheme.colorScheme.secondary,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    tonalElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level3,
    properties: DialogProperties = DialogProperties(),
    contentDescription: String? = null
) {
    // Enhanced entrance animation with coordinated elements
    val entranceProgress by ExpressiveMotion.entranceAnimation(
        visible = true,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Staggered animation for dialog elements
    val iconProgress by ExpressiveMotion.coordinatedElementAnimation(
        visible = true,
        index = 0,
        staggerDelay = ExpressiveMotion.CoordinatedStaggerDelay
    )
    
    val titleProgress by ExpressiveMotion.coordinatedElementAnimation(
        visible = true,
        index = 1,
        staggerDelay = ExpressiveMotion.CoordinatedStaggerDelay
    )
    
    val textProgress by ExpressiveMotion.coordinatedElementAnimation(
        visible = true,
        index = 2,
        staggerDelay = ExpressiveMotion.CoordinatedStaggerDelay
    )
    
    val buttonsProgress by ExpressiveMotion.coordinatedElementAnimation(
        visible = true,
        index = 3,
        staggerDelay = ExpressiveMotion.CoordinatedStaggerDelay
    )
    
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Box(
                modifier = Modifier
                    .alpha(buttonsProgress)
                    .scale(0.8f + (0.2f * buttonsProgress))
            ) {
                confirmButton()
            }
        },
        modifier = modifier
            .scale(0.8f + (0.2f * entranceProgress))
            .alpha(entranceProgress)
            .let { mod ->
                contentDescription?.let { desc ->
                    mod.semantics { this.contentDescription = desc }
                } ?: mod
            },
        dismissButton = dismissButton?.let { button ->
            {
                Box(
                    modifier = Modifier
                        .alpha(buttonsProgress)
                        .scale(0.8f + (0.2f * buttonsProgress))
                ) {
                    button()
                }
            }
        },
        icon = icon?.let { iconContent ->
            {
                Box(
                    modifier = Modifier
                        .alpha(iconProgress)
                        .scale(0.8f + (0.2f * iconProgress))
                ) {
                    iconContent()
                }
            }
        },
        title = title?.let { titleContent ->
            {
                Box(
                    modifier = Modifier
                        .alpha(titleProgress)
                        .scale(0.9f + (0.1f * titleProgress))
                ) {
                    titleContent()
                }
            }
        },
        text = text?.let { textContent ->
            {
                Box(
                    modifier = Modifier
                        .alpha(textProgress)
                        .scale(0.95f + (0.05f * textProgress))
                ) {
                    textContent()
                }
            }
        },
        shape = shape,
        containerColor = containerColor,
        iconContentColor = iconContentColor,
        titleContentColor = titleContentColor,
        textContentColor = textContentColor,
        tonalElevation = tonalElevation,
        properties = properties
    )
}

/**
 * Expressive Bottom Sheet Dialog with enhanced slide animations.
 */
@Composable
fun ExpressiveBottomSheetDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    windowInsets: WindowInsets = BottomSheetDefaults.windowInsets,
    properties: DialogProperties = DialogProperties(),
    shape: Shape = RoundedCornerShape(
        topStart = ExpressiveTokens.CornerRadius.Large,
        topEnd = ExpressiveTokens.CornerRadius.Large
    ),
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: androidx.compose.ui.unit.Dp = ExpressiveTokens.Elevation.Level1,
    contentDescription: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    // Enhanced slide-up animation
    val slideProgress by ExpressiveMotion.entranceAnimation(
        visible = true,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
    
    // Use ModalBottomSheet instead of BottomSheetDialog for Material 3 compatibility
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier
            .offset(y = (50.dp * (1f - slideProgress)))
            .alpha(slideProgress)
            .let { mod ->
                contentDescription?.let { desc ->
                    mod.semantics { this.contentDescription = desc }
                } ?: mod
            },
        dragHandle = dragHandle,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        content = content
    )
}

/**
 * Default values for ExpressiveDialog components.
 */
object ExpressiveDialogDefaults {
    
    /**
     * Default shape for dialogs.
     */
    val Shape = RoundedCornerShape(ExpressiveTokens.CornerRadius.Large)
    
    /**
     * Default elevation for dialogs.
     */
    val Elevation = ExpressiveTokens.Elevation.Level3
    
    /**
     * Default container color for dialogs.
     */
    @Composable
    fun containerColor(): Color = MaterialTheme.colorScheme.surfaceContainerHigh
    
    /**
     * Default content color for dialogs.
     */
    @Composable
    fun contentColor(): Color = MaterialTheme.colorScheme.onSurface
    
    /**
     * Default properties for dialogs with enhanced behavior.
     */
    fun properties(
        dismissOnBackPress: Boolean = true,
        dismissOnClickOutside: Boolean = true,
        usePlatformDefaultWidth: Boolean = true
    ): DialogProperties = DialogProperties(
        dismissOnBackPress = dismissOnBackPress,
        dismissOnClickOutside = dismissOnClickOutside,
        usePlatformDefaultWidth = usePlatformDefaultWidth
    )
}