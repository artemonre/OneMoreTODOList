package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

private val SWATCH_DIAMETER = 36.dp
// Keeps the tappable area at the 48x48dp minimum touch target even though the visible swatch is
// smaller - the extra space is padding around the drawn circle, not part of it.
private val SWATCH_TOUCH_TARGET = 48.dp
private val SWATCH_BORDER_WIDTH = 2.dp
private const val WEDGE_SWEEP_DEGREES = 120f

/**
 * A small circular preview of a [ColorScheme] - background, primary, and secondary each get an
 * equal pie wedge - used as a selectable "chip" for picking a color palette. A ring border marks
 * the selected swatch; unselected swatches show only the three colors.
 */
@Composable
fun PaletteSwatch(
    colorScheme: ColorScheme,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .size(SWATCH_TOUCH_TARGET)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding((SWATCH_TOUCH_TARGET - SWATCH_DIAMETER) / 2)
            .then(
                if (selected) {
                    Modifier.border(SWATCH_BORDER_WIDTH, MaterialTheme.colorScheme.primary, CircleShape)
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
    ) {
        drawArc(
            color = colorScheme.background,
            startAngle = -90f,
            sweepAngle = WEDGE_SWEEP_DEGREES,
            useCenter = true
        )
        drawArc(
            color = colorScheme.primary,
            startAngle = -90f + WEDGE_SWEEP_DEGREES,
            sweepAngle = WEDGE_SWEEP_DEGREES,
            useCenter = true
        )
        drawArc(
            color = colorScheme.secondary,
            startAngle = -90f + 2 * WEDGE_SWEEP_DEGREES,
            sweepAngle = WEDGE_SWEEP_DEGREES,
            useCenter = true
        )
    }
}

@Preview(widthDp = 48, heightDp = 48)
@Composable
private fun PaletteSwatchPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        PaletteSwatch(
            colorScheme = MaterialTheme.colorScheme,
            selected = true,
            onClick = {}
        )
    }
}
