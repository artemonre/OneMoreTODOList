package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialChipGroup
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a single-choice chip group for the current [LocalUiStyle], falling back to the plain
 * Material3 chips for any style without its own implementation.
 */
@Composable
fun <T> AppChipGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialChipGroup(options, selectedOption, onOptionSelected, label, modifier)
        // No Paper chips yet - fall back to Material.
        UiStyleOption.Paper -> MaterialChipGroup(options, selectedOption, onOptionSelected, label, modifier)
    }
}
