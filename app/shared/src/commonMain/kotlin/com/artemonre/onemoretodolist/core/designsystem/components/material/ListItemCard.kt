package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A stock Material3 [ElevatedCard] - default shape/elevation/colors from
 * [androidx.compose.material3.CardDefaults], no custom shadow or press animation. Domain-agnostic:
 * takes only an [onClick] and arbitrary [content], so any feature can use it for a clickable list
 * row without this module knowing what that row represents.
 *
 * The onClick ElevatedCard overload has no long-click, so the click lives on the content instead -
 * the card clips it, so the ripple still fills the card. Nothing visible is lost: an elevated
 * card's pressed elevation equals its resting one.
 */
@Composable
fun ListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = if (containerColor != null) {
            CardDefaults.elevatedCardColors(containerColor = containerColor, contentColor = contentColorFor(containerColor))
        } else {
            CardDefaults.elevatedCardColors()
        }
    ) {
        Box(
            modifier = Modifier
                .combinedClickable(onLongClick = onLongClick, onClick = onClick)
                .padding(AppSpacing.s)
        ) {
            content()
        }
    }
}

@Preview(widthDp = 280, heightDp = 56)
@Composable
private fun ListItemCardPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        ListItemCard(onClick = {}, modifier = Modifier.fillMaxWidth()) {
            Text("Buy groceries")
        }
    }
}
