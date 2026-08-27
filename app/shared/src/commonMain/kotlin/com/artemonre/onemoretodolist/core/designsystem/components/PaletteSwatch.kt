package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

private val SWATCH_DIAMETER = 24.dp
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
            .size(SWATCH_DIAMETER)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
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
