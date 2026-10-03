package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialChipGroup
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a single-choice chip group for the current [LocalUiStyle], falling back to the plain
 * Material3 chips for any style without its own implementation. [singleLine] keeps every chip on
 * one horizontally scrolling line instead of wrapping; [compact] makes the chips smaller.
 */
@Composable
fun <T> AppChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    compact: Boolean = false
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialChipGroup(options, selectedOption, onOptionSelected, label, modifier, singleLine, compact)
        // No Paper chips yet - fall back to Material.
        UiStyleOption.Paper -> MaterialChipGroup(options, selectedOption, onOptionSelected, label, modifier, singleLine, compact)
    }
}
