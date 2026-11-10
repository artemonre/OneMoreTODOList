package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSegmentedButtonRow
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloSegmentedControl
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
        UiStyleOption.Holo -> HoloSegmentedControl(options, selectedOption, onOptionSelected, label, modifier)
    }
}

/**
 * A compact single-choice row for picks inside a form (e.g. a repeat unit) - Material3's connected
 * segmented buttons by default, smaller than [AppSegmentedControl]. Holo has one look for both.
 */
@Composable
fun <T> AppSegmentedButtonRow(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialSegmentedButtonRow(options, selectedOption, onOptionSelected, label, modifier)
        UiStyleOption.Holo -> HoloSegmentedControl(options, selectedOption, onOptionSelected, label, modifier)
    }
}
