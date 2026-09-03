package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialFabMenu
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

data class FabMenuItem(
    val label: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/**
 * A floating action button that performs [onClick] on a tap - unchanged from a plain [AppFab] -
 * and reveals [items], a small menu of related actions, on a long press. Falls back to the plain
 * Material3 FAB menu for any [LocalUiStyle] without its own implementation.
 */
@Composable
fun AppFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    icon: ImageVector,
    items: List<FabMenuItem>,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material ->
            MaterialFabMenu(expanded, onExpandedChange, onClick, icon, items, contentDescription, modifier)
        // No Paper FAB menu yet - fall back to Material.
        UiStyleOption.Paper ->
            MaterialFabMenu(expanded, onExpandedChange, onClick, icon, items, contentDescription, modifier)
    }
}
