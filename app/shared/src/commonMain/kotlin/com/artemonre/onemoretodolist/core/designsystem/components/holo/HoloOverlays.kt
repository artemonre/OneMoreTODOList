package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.components.material.AlertDialogLayout
import com.artemonre.onemoretodolist.core.designsystem.components.material.BottomSheetLayout
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

private val DRAG_HANDLE_VERTICAL_PADDING = 16.dp

/**
 * The Holo bottom sheet: Material3's sheet and behavior, with an accent line along its top edge
 * (following the rounded corners, like the navigation bar's line) and a short accent bar for a
 * drag handle. Same parameters as
 * [com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoloBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    onExpanded: () -> Unit = {},
    topDecoration: @Composable (() -> Unit)? = null,
    topDecorationOverhang: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val density = LocalDensity.current
    val cornerRadius = (BottomSheetDefaults.ExpandedShape as? CornerBasedShape)
        ?.topStart
        ?.let { with(density) { it.toPx(Size.Zero, density).toDp() } }
        ?: 0.dp
    BottomSheetLayout(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        showDragHandle = showDragHandle,
        onExpanded = onExpanded,
        topDecoration = topDecoration,
        topDecorationOverhang = topDecorationOverhang,
        dragHandle = { HoloDragHandle() },
        sheetDecoration = Modifier.holoTopEdgeLine(cornerRadius),
        content = content
    )
}

/** The short accent bar the Holo sheet uses as its drag handle - like the stats card's accent. */
@Composable
private fun HoloDragHandle() {
    Box(
        modifier = Modifier
            .padding(vertical = DRAG_HANDLE_VERTICAL_PADDING)
            .size(HoloDefaults.HeroAccentBarWidth, HoloDefaults.HeroAccentBarThickness)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
}

/**
 * A line in [MaterialTheme.colorScheme.primary] along the top edge, following top corners of
 * [cornerRadius] down to where they meet the sides.
 */
@Composable
private fun Modifier.holoTopEdgeLine(cornerRadius: Dp): Modifier {
    val color = MaterialTheme.colorScheme.primary
    return drawWithContent {
        drawContent()
        val stroke = HoloDefaults.BorderWidth.toPx()
        // Inset by half the stroke, so all of it lands inside the sheet.
        val inset = stroke / 2
        val r = cornerRadius.toPx()
        val line = Path().apply {
            moveTo(inset, r)
            arcTo(Rect(inset, inset, 2 * r - inset, 2 * r - inset), 180f, 90f, forceMoveTo = false)
            lineTo(size.width - r, inset)
            arcTo(Rect(size.width - 2 * r + inset, inset, size.width - inset, 2 * r - inset), 270f, 90f, forceMoveTo = false)
        }
        drawPath(line, color, style = Stroke(width = stroke))
    }
}

/**
 * The Holo alert dialog: the static-card look (outline and highlighted corner), a `>` prompt
 * before the title that TalkBack skips, and the text in Holo's mono supporting style. Same
 * parameters as [com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog].
 */
@Composable
fun HoloAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier,
    dismissButton: @Composable (() -> Unit)?,
    icon: @Composable (() -> Unit)?,
    title: @Composable (() -> Unit)?,
    text: @Composable (() -> Unit)?,
    contentPadding: PaddingValues,
    buttonsTopSpacing: Dp,
    topDecoration: @Composable (() -> Unit)?,
    topDecorationOverhang: Dp,
    bottomDecoration: @Composable (() -> Unit)?,
    bottomDecorationHeight: Dp,
    properties: DialogProperties
) {
    val shape = RoundedCornerShape(HoloDefaults.HeroCardCornerRadius)
    val colors = MaterialTheme.colorScheme
    AlertDialogLayout(
        onDismissRequest, confirmButton, modifier, dismissButton, icon, title, text, contentPadding,
        buttonsTopSpacing, topDecoration, topDecorationOverhang, bottomDecoration, bottomDecorationHeight, properties,
        card = { cardModifier, content ->
            Surface(
                modifier = cardModifier
                    .clip(shape)
                    .border(HoloDefaults.BorderWidth, colors.outlineVariant, shape)
                    .holoCornerHighlight(HoloDefaults.HeroCardCornerRadius),
                shape = shape,
                color = AlertDialogDefaults.containerColor,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                content = content
            )
        },
        titlePrefix = {
            Text(
                text = HoloDefaults.PROMPT_GLYPH,
                color = colors.tertiary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Spacer(Modifier.width(AppSpacing.s))
        },
        textStyle = holoSupportingTextStyle().copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
    )
}

@Preview
@Composable
private fun HoloAlertDialogPreview() {
    AppTheme(themeConfig = ThemeConfig(uiStyle = UiStyleOption.Holo)) {
        HoloAlertDialog(
            onDismissRequest = {},
            confirmButton = { TextButton(onClick = {}) { Text("Merge") } },
            modifier = Modifier,
            dismissButton = { TextButton(onClick = {}) { Text("Cancel") } },
            icon = null,
            title = { Text("Import todos") },
            text = { Text("Add the backup's todos to yours, or replace yours with them?") },
            contentPadding = PaddingValues(AppSpacing.xl),
            buttonsTopSpacing = AppSpacing.xl,
            topDecoration = null,
            topDecorationOverhang = 0.dp,
            bottomDecoration = null,
            bottomDecorationHeight = 0.dp,
            properties = DialogProperties()
        )
    }
}
