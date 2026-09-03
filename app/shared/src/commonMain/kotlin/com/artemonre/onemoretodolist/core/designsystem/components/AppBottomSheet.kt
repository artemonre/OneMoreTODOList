package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialBottomSheet
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a modal bottom sheet for the current [LocalUiStyle], falling back to the plain
 * Material3 sheet for any style without its own implementation.
 */
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialBottomSheet(onDismissRequest, modifier, showDragHandle, content)
        // No Paper bottom sheet yet - fall back to Material.
        UiStyleOption.Paper -> MaterialBottomSheet(onDismissRequest, modifier, showDragHandle, content)
    }
}
