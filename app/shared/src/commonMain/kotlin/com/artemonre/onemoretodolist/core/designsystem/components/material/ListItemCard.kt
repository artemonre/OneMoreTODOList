package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A stock Material3 [ElevatedCard] - default shape/elevation/colors from
 * [androidx.compose.material3.CardDefaults], no custom shadow or press animation. Domain-agnostic:
 * takes only an [onClick] and arbitrary [content], so any feature can use it for a clickable list
 * row without this module knowing what that row represents.
 */
@Composable
fun ListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier
    ) {
        content()
    }
}
