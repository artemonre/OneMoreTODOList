package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialNavigationBar
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders the bottom navigation bar for the current [LocalUiStyle], falling back to the plain
 * Material3 navigation bar for any style without its own implementation.
 */
@Composable
fun <T> AppNavigationBar(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (T) -> ImageVector,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialNavigationBar(items, selectedIndex, onItemSelected, icon, label, modifier)
        // No Neumorphic or Paper navigation bar yet - fall back to Material.
        UiStyleOption.Neumorphic -> MaterialNavigationBar(items, selectedIndex, onItemSelected, icon, label, modifier)
        UiStyleOption.Paper -> MaterialNavigationBar(items, selectedIndex, onItemSelected, icon, label, modifier)
    }
}
