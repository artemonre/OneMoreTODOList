package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A stock Material3 [ShortNavigationBar] of [ShortNavigationBarItem]s - the compact 64dp bar,
 * default colors and indicator all from `ShortNavigationBarItemDefaults`, with each item's
 * [label] shown under its icon.
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
    ShortNavigationBar(modifier = modifier) {
        items.forEachIndexed { index, item ->
            ShortNavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onItemSelected(index) },
                // The label below already names the item - a description here would be read twice.
                icon = { Icon(imageVector = icon(item), contentDescription = null) },
                label = { Text(label(item)) }
            )
        }
    }
}
