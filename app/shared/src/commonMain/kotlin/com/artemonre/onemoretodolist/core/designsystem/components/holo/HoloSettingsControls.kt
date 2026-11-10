package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.toFontFamily
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

private val SEGMENT_MIN_HEIGHT = 40.dp
private val SEGMENT_CORNER_RADIUS = 8.dp
private val SWITCH_TRACK_WIDTH = 44.dp
private val SWITCH_TRACK_HEIGHT = 24.dp
private val SWITCH_THUMB_SIZE = 20.dp
private val SWITCH_THUMB_INSET = 2.dp
private val BUTTON_ICON_BADGE_SIZE = 24.dp
private val BUTTON_ICON_SIZE = 16.dp
private val BUTTON_MIN_HEIGHT = 40.dp
private val SUPPORTING_LINE_HEIGHT = 20.sp

/**
 * A Holo segmented control: separate rounded-rectangle segments in a soft track. The selected
 * segment is filled with [MaterialTheme.colorScheme.primary], the rest with its container color.
 */
@Composable
fun <T> HoloSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    val segmentShape = RoundedCornerShape(SEGMENT_CORNER_RADIUS)
    Row(
        modifier = modifier
            .clip(HoloControlShape)
            .background(colors.primaryContainer.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA))
            .border(HoloDefaults.ThinBorderWidth, colors.primary.copy(alpha = HoloDefaults.SOFT_BORDER_ALPHA), HoloControlShape)
            .padding(AppSpacing.xs)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
    ) {
        options.forEach { option ->
            val selected = option == selectedOption
            Box(
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = SEGMENT_MIN_HEIGHT)
                    .clip(segmentShape)
                    .background(if (selected) colors.primary else colors.primaryContainer)
                    .selectable(selected = selected, onClick = { onOptionSelected(option) }, role = Role.RadioButton)
                    .padding(horizontal = AppSpacing.s),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label(option),
                    maxLines = 1,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) colors.onPrimary else colors.onPrimaryContainer
                )
            }
        }
    }
}

/** A Holo switch: slimmer than Material's, a flat track with a round thumb and no outline. */
@Composable
fun HoloSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val thumbOffset by animateDpAsState(
        if (checked) SWITCH_TRACK_WIDTH - SWITCH_THUMB_SIZE - SWITCH_THUMB_INSET else SWITCH_THUMB_INSET
    )
    Box(
        modifier = modifier
            .then(
                if (onCheckedChange != null) {
                    Modifier
                        .minimumInteractiveComponentSize()
                        .toggleable(value = checked, onValueChange = onCheckedChange, enabled = enabled, role = Role.Switch)
                } else {
                    Modifier
                }
            )
            .alpha(if (enabled) 1f else HOLO_DISABLED_ALPHA),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(SWITCH_TRACK_WIDTH, SWITCH_TRACK_HEIGHT)
                .background(if (checked) colors.primary else colors.outlineVariant, CircleShape),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(SWITCH_THUMB_SIZE)
                    .background(colors.surface, CircleShape)
            )
        }
    }
}

/**
 * A Holo section title: `01 // Title`, the number in a small mono accent font. The number is
 * decoration - TalkBack reads only [title], as a heading.
 */
@Composable
fun HoloSectionTitle(
    title: String,
    number: Int?,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (number != null) {
            Text(
                text = "${number.toString().padStart(2, '0')} ${HoloDefaults.SECTION_SEPARATOR}",
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = FontOption.Monospace.toFontFamily()),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clearAndSetSemantics { }
            )
            Spacer(Modifier.width(AppSpacing.s))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { heading() }
        )
    }
}

/**
 * A Holo outlined button: a pill outline with [icon] set in a small filled circle before [text].
 */
@Composable
fun HoloOutlinedButton(
    onClick: () -> Unit,
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = AppSpacing.l)
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .border(HoloDefaults.ThinBorderWidth, colors.outline, CircleShape)
            .clickable(enabled = enabled, onClick = onClick, role = Role.Button)
            .alpha(if (enabled) 1f else HOLO_DISABLED_ALPHA)
            .defaultMinSize(minHeight = BUTTON_MIN_HEIGHT)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(BUTTON_ICON_BADGE_SIZE)
                .background(colors.onSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.surface, modifier = Modifier.size(BUTTON_ICON_SIZE))
        }
        Spacer(Modifier.width(AppSpacing.s))
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = colors.onSurface, maxLines = 1)
    }
}

/** A thin Holo divider between the parts of a section. */
@Composable
fun HoloDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, color = MaterialTheme.colorScheme.outlineVariant)
}

/** Holo's supporting text: small, in mono, with roomy lines. */
@Composable
fun holoSupportingTextStyle(): TextStyle = MaterialTheme.typography.bodySmall.copy(
    fontFamily = FontOption.Monospace.toFontFamily(),
    lineHeight = SUPPORTING_LINE_HEIGHT
)

@Preview(widthDp = 320, heightDp = 360)
@Composable
private fun HoloSettingsControlsPreview() {
    HoloSettingsControlsPreviewContent(ThemeMode.Light)
}

@Preview(widthDp = 320, heightDp = 360)
@Composable
private fun HoloSettingsControlsDarkPreview() {
    HoloSettingsControlsPreviewContent(ThemeMode.Dark)
}

@Composable
private fun HoloSettingsControlsPreviewContent(mode: ThemeMode) {
    AppTheme(themeConfig = ThemeConfig(mode = mode, uiStyle = UiStyleOption.Holo)) {
        HoloCard(modifier = Modifier.fillMaxWidth().padding(AppSpacing.s)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.m), modifier = Modifier.padding(AppSpacing.xs)) {
                HoloSectionTitle(title = "Theme", number = 1)
                HoloSegmentedControl(listOf("System", "Light", "Dark"), "System", {}, { it }, Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Archive completed todos", modifier = Modifier.weight(1f))
                    HoloSwitch(checked = true, onCheckedChange = {})
                }
                Text("When off, completing a todo deletes it.", style = holoSupportingTextStyle())
                HoloDivider()
                HoloOutlinedButton(onClick = {}, text = "Back up", icon = Icons.Filled.FileUpload)
            }
        }
    }
}
