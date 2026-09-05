package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.material.ListItemCard as MaterialListItemCard
import com.artemonre.onemoretodolist.core.designsystem.components.paper.ListItemCard as PaperListItemCard
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a clickable list-item card for the current [LocalUiStyle], falling back to the plain
 * Material3 card for any style without its own implementation. Domain-agnostic: any feature can
 * use this for a card-shaped list row without this module knowing what that row represents.
 */
@Composable
fun AppListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialListItemCard(onClick, modifier, content)
        UiStyleOption.Paper -> PaperListItemCard(onClick, modifier, content)
    }
}
