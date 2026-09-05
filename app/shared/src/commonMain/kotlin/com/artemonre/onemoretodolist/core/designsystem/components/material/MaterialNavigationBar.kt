package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A stock Material3 [NavigationBar] of [NavigationBarItem]s - default colors and indicator all
 * from `NavigationBarItemDefaults`, no customizations.
 */
@Composable
fun <T> MaterialNavigationBar(
    items: List<T>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    icon: (T) -> ImageVector,
    label: (T) -> String,
    modifier: Modifier = Modifier
) {
    NavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                icon = { Icon(imageVector = icon(item), contentDescription = label(item)) },
                label = { Text(label(item)) }
            )
        }
    }
}
