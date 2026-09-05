package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.BackHandler
import com.artemonre.onemoretodolist.core.designsystem.components.FabMenuItem

// A hand-rolled trigger + item list, deliberately not built on Material3's
// FloatingActionButtonMenu/FloatingActionButtonMenuItem: that component sizes its own combined
// bounds (button + revealed items) as one block, so wherever it's placed, its trigger visibly
// shifts as items appear/disappear. Here the trigger is a fully independent Box, always the same
// fixed size regardless of whether the item list beside it is showing - it never moves.
private val FAB_TRIGGER_SIZE = 56.dp
private val FAB_TRIGGER_CORNER_RADIUS = 16.dp
private val FAB_TRIGGER_ELEVATION = 6.dp
private val FAB_MENU_ITEM_SPACING = 8.dp
private val FAB_MENU_ITEM_CORNER_RADIUS = 16.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MaterialFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    icon: ImageVector,
    items: List<FabMenuItem>,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = -(FAB_TRIGGER_SIZE + FAB_MENU_ITEM_SPACING)),
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom)
        ) {
            FabMenuItemColumn(
                items = items,
                onItemSelected = { item ->
                    item.onClick()
                    onExpandedChange(false)
                }
            )
        }

        val containerColor = MaterialTheme.colorScheme.primaryContainer
        Surface(
            modifier = Modifier
                .size(FAB_TRIGGER_SIZE)
                .combinedClickable(
                    onClick = { if (expanded) onExpandedChange(false) else onClick() },
                    onLongClick = { onExpandedChange(true) },
                    onLongClickLabel = "Open menu",
                    role = Role.Button
                ),
            shape = RoundedCornerShape(FAB_TRIGGER_CORNER_RADIUS),
            color = containerColor,
            contentColor = contentColorFor(containerColor),
            shadowElevation = FAB_TRIGGER_ELEVATION
        ) {
            val rotation by animateFloatAsState(if (expanded) 45f else 0f)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(FAB_TRIGGER_SIZE)) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation }
                )
            }
        }
    }
}

@Composable
private fun FabMenuItemColumn(
    items: List<FabMenuItem>,
    onItemSelected: (FabMenuItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(FAB_MENU_ITEM_SPACING)
    ) {
        items.forEach { item ->
            val containerColor = if (item.enabled) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val contentColor = if (item.enabled) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            }
            Surface(
                onClick = { onItemSelected(item) },
                enabled = item.enabled,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(FAB_MENU_ITEM_CORNER_RADIUS),
                color = containerColor,
                contentColor = contentColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = item.icon, contentDescription = null)
                    Spacer(Modifier.width(16.dp))
                    Text(text = item.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
