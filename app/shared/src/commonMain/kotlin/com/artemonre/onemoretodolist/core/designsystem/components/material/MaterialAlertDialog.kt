package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.takeOrElse
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Copied from Material3's AlertDialog internals (private there), touch-sized variant.
private val ICON_BOTTOM_SPACING = 16.dp
private val TITLE_BOTTOM_SPACING = 16.dp
private val BUTTONS_MAIN_AXIS_SPACING = 8.dp
private val BUTTONS_CROSS_AXIS_SPACING = 8.dp

object MaterialAlertDialogDefaults {
    /** Material3's built-in AlertDialog padding. */
    val ContentPadding = PaddingValues(all = 24.dp)

    /** Material3's built-in gap between the text and the buttons. */
    val ButtonsTopSpacing = 24.dp
}

/**
 * A Material3 alert dialog that looks the same as the stock
 * [androidx.compose.material3.AlertDialog], rebuilt on [BasicAlertDialog] so its inner
 * [contentPadding] and the text-to-buttons gap ([buttonsTopSpacing]) can be customized - the stock
 * one hardcodes both.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = MaterialAlertDialogDefaults.ContentPadding,
    buttonsTopSpacing: Dp = MaterialAlertDialogDefaults.ButtonsTopSpacing,
    properties: DialogProperties = DialogProperties()
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = properties
    ) {
        Surface(
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides AlertDialogDefaults.iconContentColor) {
                        Box(
                            Modifier
                                .padding(bottom = ICON_BOTTOM_SPACING)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
                title?.let {
                    ProvideContentColorTextStyle(
                        contentColor = AlertDialogDefaults.titleContentColor,
                        textStyle = MaterialTheme.typography.headlineSmall
                    ) {
                        Box(
                            // Centered when an icon is present, same as stock.
                            Modifier
                                .padding(bottom = TITLE_BOTTOM_SPACING)
                                .align(if (icon == null) Alignment.Start else Alignment.CenterHorizontally)
                        ) {
                            title()
                        }
                    }
                }
                text?.let {
                    ProvideContentColorTextStyle(
                        contentColor = AlertDialogDefaults.textContentColor,
                        textStyle = MaterialTheme.typography.bodyMedium
                    ) {
                        Box(
                            Modifier
                                .weight(weight = 1f, fill = false)
                                .padding(bottom = buttonsTopSpacing)
                                .align(Alignment.Start)
                        ) {
                            text()
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    ProvideContentColorTextStyle(
                        contentColor = MaterialTheme.colorScheme.primary,
                        textStyle = MaterialTheme.typography.labelLarge
                    ) {
                        DialogButtons(confirmButton = confirmButton, dismissButton = dismissButton)
                    }
                }
            }
        }
    }
}

// Confirm comes after dismiss when side by side, but on top when the row wraps - done by laying
// the FlowRow out in the flipped direction, same as stock.
@Composable
private fun DialogButtons(
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)?
) {
    val buttonPaddingFromMinTouchTarget =
        LocalMinimumInteractiveComponentSize.current.takeOrElse { 0.dp } - ButtonDefaults.MinHeight
    val originalLayoutDirection = LocalLayoutDirection.current
    val flipped = when (originalLayoutDirection) {
        LayoutDirection.Ltr -> LayoutDirection.Rtl
        LayoutDirection.Rtl -> LayoutDirection.Ltr
    }
    CompositionLocalProvider(LocalLayoutDirection provides flipped) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(BUTTONS_MAIN_AXIS_SPACING),
            verticalArrangement = Arrangement.spacedBy(
                (BUTTONS_CROSS_AXIS_SPACING - buttonPaddingFromMinTouchTarget)
                    .coerceIn(0.dp, BUTTONS_CROSS_AXIS_SPACING)
            )
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides originalLayoutDirection) {
                confirmButton()
                dismissButton?.invoke()
            }
        }
    }
}

@Composable
private fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content
    )
}

@Preview
@Composable
private fun MaterialAlertDialogPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        MaterialAlertDialog(
            onDismissRequest = {},
            title = { Text("Title") },
            text = { Text("Supporting text that explains what this dialog is about.") },
            confirmButton = { TextButton(onClick = {}) { Text("Confirm") } },
            dismissButton = { TextButton(onClick = {}) { Text("Cancel") } }
        )
    }
}
