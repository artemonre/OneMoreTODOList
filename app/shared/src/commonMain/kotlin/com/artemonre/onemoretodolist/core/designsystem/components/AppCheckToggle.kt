package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialCheckbox
import com.artemonre.onemoretodolist.core.designsystem.components.paper.PaperCheckbox
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders the checkbox for the current [LocalUiStyle], falling back to [MaterialCheckbox] for
 * any style without its own implementation.
 */
@Composable
fun AppCheckToggle(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialCheckbox(checked, onCheckedChange, modifier)
        UiStyleOption.Paper -> PaperCheckbox(checked, onCheckedChange, modifier)
    }
}
