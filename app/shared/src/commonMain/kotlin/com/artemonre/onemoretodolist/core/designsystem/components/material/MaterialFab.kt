package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * A stock Material3 [FloatingActionButton] - default shape, colors, and elevation all from
 * `FloatingActionButtonDefaults`, no customizations.
 */
@Composable
fun MaterialFab(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(onClick = onClick, modifier = modifier) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}
