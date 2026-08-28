package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A stock Material3 [Checkbox] - default colors, no customizations.
 */
@Composable
fun MaterialCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier
    )
}
