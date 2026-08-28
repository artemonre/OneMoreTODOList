package com.artemonre.onemoretodolist.core.designsystem.components.neumorphic

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode

// A full-pill shape - unlike NeumorphicSegmentedControl's segments, chips aren't joined into one
// continuous bar, so every chip gets fully rounded corners rather than just the group's ends.
private val CHIP_SHAPE = RoundedCornerShape(percent = 50)

// Same shadow recipe as NeumorphicSegmentedControl (see its withOklabLightnessDelta doc for why
// an additive Oklab lightness step, not a lerp-toward-black/white) - kept as separate constants
// rather than shared ones since a chip's smaller footprint can use its own tuning independently.
private val OUTER_SHADOW_RADIUS = 3.dp
private val OUTER_SHADOW_OFFSET = 2.dp
private val OUTER_SHADOW_SPREAD = (-0.5).dp
private val INNER_SHADOW_RADIUS = 3.dp
private val INNER_SHADOW_OFFSET = 2.dp
private const val OUTER_SHADOW_ALPHA = 0.36f
private const val INNER_SHADOW_ALPHA = 0.45f
private const val SHADOW_LIGHTNESS_DELTA = 0.16f

/**
 * A single-choice control styled like a neumorphic ("soft UI") chip group: unselected chips sit
 * raised off the surface behind them, and the selected chip sits pressed into it - the same
 * light+dark shadow-pair technique as [NeumorphicSegmentedControl], just applied to individually
 * pill-shaped, wrapping chips instead of one joined bar. Wraps to a new row via [FlowRow] when
 * the chips don't fit on one line.
 */
@Composable
fun <T> NeumorphicChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        options.forEach { option ->
            NeumorphicChip(
                label = label(option),
                isSelected = option == selectedOption,
                onClick = { onOptionSelected(option) }
            )
        }
    }
}

@Composable
private fun NeumorphicChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(if (isSelected) 1f else 0f)
    val surfaceColor = MaterialTheme.colorScheme.background
    val darkShadowColor = surfaceColor.withOklabLightnessDelta(-SHADOW_LIGHTNESS_DELTA)
    val lightShadowColor = surfaceColor.withOklabLightnessDelta(SHADOW_LIGHTNESS_DELTA)
    val shape: Shape = CHIP_SHAPE

    Text(
        text = label,
        color = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        style = MaterialTheme.typography.labelLarge,
        modifier = modifier
            .dropShadow(shape) {
                color = darkShadowColor
                radius = OUTER_SHADOW_RADIUS.toPx()
                spread = OUTER_SHADOW_SPREAD.toPx()
                offset = Offset(OUTER_SHADOW_OFFSET.toPx(), OUTER_SHADOW_OFFSET.toPx())
                alpha = OUTER_SHADOW_ALPHA * (1f - progress)
            }
            .dropShadow(shape) {
                color = lightShadowColor
                radius = OUTER_SHADOW_RADIUS.toPx()
                spread = OUTER_SHADOW_SPREAD.toPx()
                offset = Offset(-OUTER_SHADOW_OFFSET.toPx(), -OUTER_SHADOW_OFFSET.toPx())
                alpha = OUTER_SHADOW_ALPHA * (1f - progress)
            }
            .background(surfaceColor, shape)
            .innerShadow(shape) {
                color = darkShadowColor
                radius = INNER_SHADOW_RADIUS.toPx()
                offset = Offset(INNER_SHADOW_OFFSET.toPx(), INNER_SHADOW_OFFSET.toPx())
                alpha = INNER_SHADOW_ALPHA * progress
            }
            .innerShadow(shape) {
                color = lightShadowColor
                radius = INNER_SHADOW_RADIUS.toPx()
                offset = Offset(-INNER_SHADOW_OFFSET.toPx(), -INNER_SHADOW_OFFSET.toPx())
                alpha = INNER_SHADOW_ALPHA * progress
            }
            .clip(shape)
            .clickable(
                interactionSource = null,
                indication = null,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Preview
@Composable
private fun NeumorphicChipGroupPreview() {
    AppTheme(themeConfig = ThemeConfig(mode = ThemeMode.Light)) {
        NeumorphicChipGroup(
            options = listOf("Material", "Neumorphic", "Paper"),
            selectedOption = "Material",
            onOptionSelected = {},
            label = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Preview
@Composable
private fun NeumorphicChipGroupDarkPreview() {
    AppTheme(themeConfig = ThemeConfig(mode = ThemeMode.Dark)) {
        NeumorphicChipGroup(
            options = listOf("Material", "Neumorphic", "Paper"),
            selectedOption = "Material",
            onOptionSelected = {},
            label = { it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}
