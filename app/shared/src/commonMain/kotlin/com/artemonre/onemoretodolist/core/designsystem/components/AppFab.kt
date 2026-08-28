package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialFab
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a floating action button for the current [LocalUiStyle], falling back to the plain
 * Material3 FAB for any style without its own implementation. Used for both the "add" action and
 * the "scroll to top" action - they're the same widget, just a different icon/onClick.
 */
@Composable
fun AppFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialFab(onClick, icon, contentDescription, modifier)
        // No Paper FAB yet - fall back to Material.
        UiStyleOption.Paper -> MaterialFab(onClick, icon, contentDescription, modifier)
    }
}
