package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A single-choice control using a Material3 connected button group - a stock [ButtonGroup] of
 * [ToggleButton]s with [ButtonGroupDefaults]'s connected leading/middle/trailing shapes, no
 * customizations. See https://m3.material.io/components/button-groups/overview.
 *
 * [ButtonGroup]'s non-deprecated overload only exposes a `toggleableItem` DSL that renders
 * standalone (non-connected) toggle buttons, so the connected leading/middle/trailing shapes are
 * supplied via `customItem` instead, which still accepts an arbitrary composable per item.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> MaterialSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    ButtonGroup(
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        modifier = modifier
    ) {
        options.forEachIndexed { index, option ->
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    ToggleButton(
                        checked = option == selectedOption,
                        onCheckedChange = { onOptionSelected(option) },
                        shapes = when (index) {
                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                            options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                        },
                        interactionSource = interactionSource,
                        // No Modifier.animateWidth() here - per the M3 guidelines, growing the
                        // pressed button and compressing its neighbors on press is a *standard*
                        // button group behavior. A connected group (this one, replacing the old
                        // segmented button) only morphs the pressed button's own shape.
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(label(option))
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(label(option)) },
                        onClick = {
                            onOptionSelected(option)
                            menuState.dismiss()
                        }
                    )
                }
            )
        }
    }
}

@Preview(widthDp = 360, heightDp = 88)
@Composable
private fun MaterialSegmentedControlPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        MaterialSegmentedControl(
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
