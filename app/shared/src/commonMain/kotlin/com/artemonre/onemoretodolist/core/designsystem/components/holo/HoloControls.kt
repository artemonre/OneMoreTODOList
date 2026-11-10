package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.material.ChipGroupLayout
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalAppIcons
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

// Drawn smaller than Material's 24dp box; the touch area stays the full 48dp either way.
private val CHECKBOX_SIZE = 20.dp
private val CONTROL_MIN_HEIGHT = 32.dp

/** A Holo checkbox: a square with a thick outline, filled with the check when [checked]. */
@Composable
fun HoloCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(HoloDefaults.CheckboxCornerRadius)
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .then(
                if (onCheckedChange != null) {
                    Modifier
                        .minimumInteractiveComponentSize()
                        .toggleable(value = checked, onValueChange = onCheckedChange, role = Role.Checkbox)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(CHECKBOX_SIZE)
                .then(
                    if (checked) {
                        Modifier.background(colors.primary, shape)
                    } else {
                        Modifier.border(HoloDefaults.BorderWidth, colors.onSurfaceVariant, shape)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(imageVector = LocalAppIcons.current.checkIcon, contentDescription = null, tint = colors.onPrimary)
            }
        }
    }
}

/** A Holo single-choice chip: a small outlined rounded rectangle. */
@Composable
private fun HoloChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .holoControl(selected)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .defaultMinSize(minHeight = CONTROL_MIN_HEIGHT)
            .padding(horizontal = if (compact) AppSpacing.m else AppSpacing.l),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            maxLines = 1,
            color = holoControlContentColor(selected),
            style = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * A single-choice row of Holo chips - same layout rules as
 * [com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialChipGroup].
 */
@Composable
fun <T> HoloChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    compact: Boolean = false
) {
    ChipGroupLayout(singleLine = singleLine, modifier = modifier) {
        options.forEach { option ->
            HoloChip(
                selected = option == selectedOption,
                onClick = { onOptionSelected(option) },
                label = label(option),
                compact = compact
            )
        }
    }
}

/** A Holo button with the look of a selected [HoloChipGroup] chip. */
@Composable
fun HoloTonalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.l),
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .holoControl(selected = true)
            .clickable(onClick = onClick, role = Role.Button)
            .defaultMinSize(minHeight = CONTROL_MIN_HEIGHT)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides holoControlContentColor(selected = true)) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}

/**
 * A Holo main-action button: a rounded rectangle filled with [MaterialTheme.colorScheme.primary],
 * like a selected segment.
 */
@Composable
fun HoloButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.l),
    content: @Composable RowScope.() -> Unit
) {
    // Material3's filled-button colors, disabled state included.
    val colors = ButtonDefaults.buttonColors()
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(HoloControlShape)
            .background(if (enabled) colors.containerColor else colors.disabledContainerColor)
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .defaultMinSize(minHeight = CONTROL_MIN_HEIGHT)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides if (enabled) colors.contentColor else colors.disabledContentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}

/**
 * A Holo secondary-action button: Material3's outlined-button colors and border, disabled state
 * included, in the same rounded rectangle and size as [HoloButton] - so a secondary action never
 * looks bigger than the main one beside it.
 */
@Composable
fun HoloTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    val colors = ButtonDefaults.outlinedButtonColors()
    val border = ButtonDefaults.outlinedButtonBorder(enabled)
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(HoloControlShape)
            .border(border, HoloControlShape)
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .defaultMinSize(minHeight = CONTROL_MIN_HEIGHT)
            .padding(horizontal = AppSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides if (enabled) colors.contentColor else colors.disabledContentColor) {
            ProvideTextStyle(MaterialTheme.typography.labelLarge) {
                content()
            }
        }
    }
}

/**
 * A Holo tag label: `# NAME` in capitals with a soft outline. TalkBack reads [text] as given,
 * without the glyph or the capitals.
 */
@Composable
fun HoloTagChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    val shape = MaterialTheme.shapes.small
    Row(
        modifier = modifier
            .clearAndSetSemantics { contentDescription = text }
            .background(containerColor, shape)
            .border(HoloDefaults.ThinBorderWidth, contentColor.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA), shape)
            .padding(horizontal = AppSpacing.s, vertical = AppSpacing.xs)
    ) {
        val style = MaterialTheme.typography.labelSmall.copy(letterSpacing = HoloDefaults.LabelLetterSpacing)
        Text(text = HoloDefaults.TAG_GLYPH, style = style, color = contentColor)
        Spacer(Modifier.width(AppSpacing.xs))
        Text(
            text = text.uppercase(),
            style = style,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(widthDp = 320, heightDp = 200)
@Composable
private fun HoloControlsPreview() {
    HoloControlsPreviewContent(ThemeMode.Light)
}

@Preview(widthDp = 320, heightDp = 200)
@Composable
private fun HoloControlsDarkPreview() {
    HoloControlsPreviewContent(ThemeMode.Dark)
}

@Composable
private fun HoloControlsPreviewContent(mode: ThemeMode) {
    AppTheme(themeConfig = ThemeConfig(mode = mode, uiStyle = UiStyleOption.Holo)) {
        Column(
            verticalArrangement = Arrangement.spacedBy(AppSpacing.s),
            modifier = Modifier.padding(AppSpacing.s)
        ) {
            HoloChipGroup(listOf("Active (15)", "Done (46)"), "Active (15)", {}, { it }, singleLine = true)
            HoloTonalButton(onClick = {}) { Text("Manual") }
            Row {
                HoloCheckbox(checked = false, onCheckedChange = {})
                HoloCheckbox(checked = true, onCheckedChange = {})
            }
            HoloTagChip(
                "Money",
                MaterialTheme.colorScheme.tertiaryContainer,
                MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
