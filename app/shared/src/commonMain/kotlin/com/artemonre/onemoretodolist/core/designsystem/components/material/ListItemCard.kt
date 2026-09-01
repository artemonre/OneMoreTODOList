package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Matches CardDefaults.elevatedShape's corner radius (Material3's default "medium" shape) so
// content never sits visually clipped by the rounded corners.
private val CARD_CONTENT_PADDING = 12.dp

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
        Box(modifier = Modifier.padding(CARD_CONTENT_PADDING)) {
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
