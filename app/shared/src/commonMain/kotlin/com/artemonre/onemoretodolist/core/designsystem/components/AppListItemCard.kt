package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.CustomAccessibilityAction
import com.artemonre.onemoretodolist.core.designsystem.components.material.ListItemCard as MaterialListItemCard
import com.artemonre.onemoretodolist.core.designsystem.components.paper.ListItemCard as PaperListItemCard
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

// The padding every list-item card style puts around its content - shared so content-level
// decorations (like the todo cards' cloud texture) can reach the card's real edges.
val AppListItemCardContentPadding = AppSpacing.s

/**
 * Renders a clickable list-item card for the current [LocalUiStyle], falling back to the plain
 * Material3 card for any style without its own implementation. Domain-agnostic: any feature can
 * use this for a card-shaped list row without this module knowing what that row represents.
 *
 * [containerColor] overrides the card's default background (and picks a matching content color
 * for whatever's inside) when non-null - e.g. a caller drawing attention to one particular row.
 * [customActions] are offered by accessibility services (TalkBack's actions menu) on the card's
 * click target - for anything the row otherwise only reaches by gesture, such as swipe or drag.
 */
@Composable
fun AppListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    customActions: List<CustomAccessibilityAction> = emptyList(),
    content: @Composable () -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialListItemCard(onClick, modifier, onLongClick, containerColor, customActions, content)
        UiStyleOption.Paper -> PaperListItemCard(onClick, modifier, onLongClick, containerColor, customActions, content)
    }
}
