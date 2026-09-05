package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a single-choice segmented control for the current [LocalUiStyle], falling back to the
 * plain Material3 control for any style without its own implementation.
 */
@Composable
fun <T> AppSegmentedControl(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialSegmentedControl(options, selectedOption, onOptionSelected, label, modifier)
        // No Paper segmented control yet - fall back to Material.
        UiStyleOption.Paper -> MaterialSegmentedControl(options, selectedOption, onOptionSelected, label, modifier)
    }
}
