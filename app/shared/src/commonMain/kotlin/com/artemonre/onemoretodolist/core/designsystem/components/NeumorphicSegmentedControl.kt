package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

private val SEGMENT_SHAPE_RADIUS = 12.dp

// A wide blur bleeds outward on every side of the shape regardless of offset direction, which is
// exactly what a normal elevated card's shadow looks like. Keeping the blur tight and pulling the
// shadow's own footprint in with a small negative spread makes it hug the edge instead of
// surrounding the whole shape like a halo, so it reads as the surface itself bending rather than
// a separate object floating above it.
private val OUTER_SHADOW_RADIUS = 3.dp
private val OUTER_SHADOW_OFFSET = 2.dp
private val OUTER_SHADOW_SPREAD = (-1).dp
private val INNER_SHADOW_RADIUS = 3.dp
private val INNER_SHADOW_OFFSET = 2.dp
private const val OUTER_DARK_ALPHA = 0.22f
private const val OUTER_LIGHT_ALPHA = 0.35f
private const val INNER_DARK_ALPHA = 0.32f
private const val INNER_LIGHT_ALPHA = 0.4f

// Shadow tints are mixed from the surface color itself rather than pure black/white - a shadow
// that shares the surface's hue reads as the same material bending, not a separate card floating
// above it with a cast shadow. Both directions share one fraction rather than a dark/light pair -
// background is now roughly centered (mid-tone in both themes, see Color.kt), so light and dark
// headroom are roughly mirror images of each other; a single shared fraction keeps the dominant
// shadow corner's swing consistent between light and dark theme instead of one theme's blend
// eating a much larger slice of its (larger) headroom than the other's.
private const val SHADOW_BLEND = 0.35f

// The segment fill is a hair lighter than the surrounding surface, constant regardless of
// selection - it reads as material that's simply closer to the light, not a state cue. State is
// communicated entirely by the shadow pair below; the fill doesn't participate.
private const val FILL_LIGHT_BLEND = 0.05f

/**
 * A single-choice control styled like a neumorphic ("soft UI") toggle group: unselected segments
 * sit raised off the surface behind them (a light+dark pair of outer drop shadows from a simulated
 * top-left light source), and the selected segment sits pressed into it (the same light+dark pair
 * as inner shadows instead). The two are cross-faded by an animated float, so switching selection
 * animates smoothly from "popped out" to "popped in".
 *
 * The segment fill is a flat tint a hair lighter than the surface it visually sits on (here,
 * [MaterialTheme.colorScheme]'s background), constant across selection - the shadows are what
 * give it shape and communicate state, not the fill.
 */
@Composable
fun <T> NeumorphicSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        options.forEachIndexed { index, option ->
            NeumorphicSegment(
                label = label(option),
                isSelected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                shape = segmentShape(index = index, lastIndex = options.lastIndex),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Only the group's outer edges are rounded - a middle segment is a plain rectangle, and a lone
// segment (list of one) gets all four corners rounded.
private fun segmentShape(index: Int, lastIndex: Int): Shape = when {
    lastIndex == 0 -> RoundedCornerShape(SEGMENT_SHAPE_RADIUS)
    index == 0 -> RoundedCornerShape(
        topStart = SEGMENT_SHAPE_RADIUS,
        bottomStart = SEGMENT_SHAPE_RADIUS
    )
    index == lastIndex -> RoundedCornerShape(
        topEnd = SEGMENT_SHAPE_RADIUS,
        bottomEnd = SEGMENT_SHAPE_RADIUS
    )
    else -> RoundedCornerShape(0.dp)
}

@Composable
private fun NeumorphicSegment(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(if (isSelected) 1f else 0f)
    val surfaceColor = MaterialTheme.colorScheme.background
    val darkShadowColor = lerp(surfaceColor, Color.Black, SHADOW_BLEND)
    val lightShadowColor = lerp(surfaceColor, Color.White, SHADOW_BLEND)
    val fillColor = lerp(surfaceColor, Color.White, FILL_LIGHT_BLEND)

    Text(
        text = label,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        style = MaterialTheme.typography.labelLarge,
        textAlign = TextAlign.Center,
        modifier = modifier
            .dropShadow(shape) {
                color = darkShadowColor
                radius = OUTER_SHADOW_RADIUS.toPx()
                spread = OUTER_SHADOW_SPREAD.toPx()
                offset = Offset(OUTER_SHADOW_OFFSET.toPx(), OUTER_SHADOW_OFFSET.toPx())
                alpha = OUTER_DARK_ALPHA * (1f - progress)
            }
            .dropShadow(shape) {
                color = lightShadowColor
                radius = OUTER_SHADOW_RADIUS.toPx()
                spread = OUTER_SHADOW_SPREAD.toPx()
                offset = Offset(-OUTER_SHADOW_OFFSET.toPx(), -OUTER_SHADOW_OFFSET.toPx())
                alpha = OUTER_LIGHT_ALPHA * (1f - progress)
            }
            .background(fillColor, shape)
            .innerShadow(shape) {
                color = darkShadowColor
                radius = INNER_SHADOW_RADIUS.toPx()
                offset = Offset(INNER_SHADOW_OFFSET.toPx(), INNER_SHADOW_OFFSET.toPx())
                alpha = INNER_DARK_ALPHA * progress
            }
            .innerShadow(shape) {
                color = lightShadowColor
                radius = INNER_SHADOW_RADIUS.toPx()
                offset = Offset(-INNER_SHADOW_OFFSET.toPx(), -INNER_SHADOW_OFFSET.toPx())
                alpha = INNER_LIGHT_ALPHA * progress
            }
            .clip(shape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(vertical = 14.dp)
    )
}

@Preview
@Composable
private fun NeumorphicSegmentedControlPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        NeumorphicSegmentedControl(
            options = listOf("System", "Light", "Dark"),
            selectedOption = "Light",
            onOptionSelected = {},
            label = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
