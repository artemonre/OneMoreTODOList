package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    onExpanded: () -> Unit = {},
    // See MaterialBottomSheet.
    topDecoration: @Composable (() -> Unit)? = null,
    topDecorationOverhang: Dp = 0.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialBottomSheet(onDismissRequest, modifier, showDragHandle, onExpanded, topDecoration, topDecorationOverhang, content)
        // No Paper bottom sheet yet - fall back to Material.
        UiStyleOption.Paper -> MaterialBottomSheet(onDismissRequest, modifier, showDragHandle, onExpanded, topDecoration, topDecorationOverhang, content)
    }
}
