package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.artemonre.onemoretodolist.core.designsystem.components.material.ListItemCard as MaterialListItemCard
import com.artemonre.onemoretodolist.core.designsystem.components.paper.ListItemCard as PaperListItemCard
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a clickable list-item card for the current [LocalUiStyle], falling back to the plain
 * Material3 card for any style without its own implementation. Domain-agnostic: any feature can
 * use this for a card-shaped list row without this module knowing what that row represents.
 *
 * [containerColor] overrides the card's default background (and picks a matching content color
 * for whatever's inside) when non-null - e.g. a caller drawing attention to one particular row.
 */
@Composable
fun AppListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    content: @Composable () -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialListItemCard(onClick, modifier, containerColor, content)
        UiStyleOption.Paper -> PaperListItemCard(onClick, modifier, containerColor, content)
    }
}
