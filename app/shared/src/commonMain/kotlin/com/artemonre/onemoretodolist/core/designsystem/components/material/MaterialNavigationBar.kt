package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.Icon
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A stock Material3 [ShortNavigationBar] of [ShortNavigationBarItem]s - the compact 64dp bar,
 * default colors and indicator all from `ShortNavigationBarItemDefaults`. Icons only - [label] is
 * just the icon's content description.
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
                icon = { Icon(imageVector = icon(item), contentDescription = label(item)) },
                label = null
            )
        }
    }
}
