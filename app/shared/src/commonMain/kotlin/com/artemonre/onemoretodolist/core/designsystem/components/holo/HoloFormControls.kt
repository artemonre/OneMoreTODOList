package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.material.CounterLayout
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialOutlinedTextField
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

private val SEGMENT_CHECK_SIZE = 16.dp
private val COUNTER_PILL_INSET = 4.dp
// Shorter than a 48dp touch target - Compose still widens the tap area of a small clickable to
// 48dp, so only the look shrinks.
private val SEGMENT_ROW_HEIGHT = 40.dp

/**
 * A Holo text field: Material3's outlined field - its colors and label behavior - in Holo's
 * rounded-rectangle shape. Same parameters as [MaterialOutlinedTextField].
 */
@Composable
fun HoloTextField(
    state: TextFieldState,
    modifier: Modifier,
    readOnly: Boolean,
    label: (@Composable () -> Unit)?,
    placeholder: (@Composable () -> Unit)?,
    trailingIcon: (@Composable () -> Unit)?,
    keyboardOptions: KeyboardOptions,
    onKeyboardAction: KeyboardActionHandler?,
    lineLimits: TextFieldLineLimits,
    compact: Boolean
) {
    MaterialOutlinedTextField(
        state, modifier, readOnly, label, placeholder, trailingIcon, keyboardOptions, onKeyboardAction, lineLimits, compact,
        shape = HoloControlShape
    )
}

/**
 * Holo's compact single-choice row for picks inside a form: connected segments in one rounded
 * rectangle with thin lines between them, and a check mark on the selected one. Colors follow
 * Material3's segmented buttons: [MaterialTheme.colorScheme.outline] lines, a
 * [MaterialTheme.colorScheme.secondaryContainer] selected segment on a
 * [MaterialTheme.colorScheme.surface] row.
 */
@Composable
fun <T> HoloSegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(HoloControlShape)
            .border(HoloDefaults.ThinBorderWidth, colors.outline, HoloControlShape)
            .selectableGroup()
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) VerticalDivider(thickness = HoloDefaults.ThinBorderWidth, color = colors.outline)
            val selected = option == selectedOption
            val contentColor = holoControlContentColor(selected)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .background(if (selected) colors.secondaryContainer else colors.surface)
                    .selectable(selected = selected, onClick = { onOptionSelected(option) }, role = Role.RadioButton)
                    .heightIn(min = SEGMENT_ROW_HEIGHT)
                    .padding(horizontal = AppSpacing.xs),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(SEGMENT_CHECK_SIZE)
                    )
                    Spacer(Modifier.width(AppSpacing.xs))
                }
                Text(text = label(option), style = MaterialTheme.typography.labelLarge, color = contentColor, maxLines = 1)
            }
        }
    }
}

/**
 * Holo's counter: the same number between − and + as Material's, with the same colors, in a pill
 * that's 8dp slimmer than the buttons - their tap areas stay the same.
 * Same parameters as [com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialCounter].
 */
@Composable
fun HoloCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    modifier: Modifier,
    decreaseEnabled: Boolean,
    increaseEnabled: Boolean
) {
    CounterLayout(
        value, onDecrease, onIncrease, decreaseDescription, increaseDescription, modifier, decreaseEnabled, increaseEnabled,
        shape = CircleShape,
        borderInset = COUNTER_PILL_INSET
    )
}

@Preview(widthDp = 320, heightDp = 240)
@Composable
private fun HoloFormControlsPreview() {
    HoloFormControlsPreviewContent(ThemeMode.Light)
}

@Preview(widthDp = 320, heightDp = 240)
@Composable
private fun HoloFormControlsDarkPreview() {
    HoloFormControlsPreviewContent(ThemeMode.Dark)
}

@Composable
private fun HoloFormControlsPreviewContent(mode: ThemeMode) {
    AppTheme(themeConfig = ThemeConfig(mode = mode, uiStyle = UiStyleOption.Holo)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.m), modifier = Modifier.padding(AppSpacing.l)) {
            HoloTextField(
                state = rememberTextFieldState(),
                modifier = Modifier.fillMaxWidth(),
                readOnly = false,
                label = { Text("Todo") },
                placeholder = { Text("What needs doing?") },
                trailingIcon = null,
                keyboardOptions = KeyboardOptions.Default,
                onKeyboardAction = null,
                lineLimits = TextFieldLineLimits.Default,
                compact = true
            )
            HoloCounter(
                value = 1,
                onDecrease = {},
                onIncrease = {},
                decreaseDescription = "Fewer",
                increaseDescription = "More",
                modifier = Modifier,
                decreaseEnabled = false,
                increaseEnabled = true
            )
            HoloSegmentedButtonRow(
                options = listOf("Day", "Week", "Month", "Year"),
                selectedOption = "Day",
                onOptionSelected = {},
                label = { it },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
