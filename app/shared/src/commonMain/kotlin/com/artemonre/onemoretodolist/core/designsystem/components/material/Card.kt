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

// Matches CardDefaults.elevatedShape's corner radius, same as ListItemCard's content padding.
private val CARD_CONTENT_PADDING = 12.dp

/**
 * A stock Material3 [ElevatedCard] with no click handling - a static container for arbitrary
 * [content]. Domain-agnostic: any feature can use it to group related content into a card without
 * this module knowing what that content represents.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(modifier = modifier) {
        Box(modifier = Modifier.padding(CARD_CONTENT_PADDING)) {
            content()
        }
    }
}

@Preview(widthDp = 280, heightDp = 56)
@Composable
private fun CardPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text("Section content")
        }
    }
}
