package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Matches PaperListItemCardShape's corner radius, so content never sits visually clipped.
private val CARD_CONTENT_PADDING = 4.dp

// Same elevation as ListItemCard, minus its press-driven flatten - nothing to press on a static
// container.
private val CARD_ELEVATION = 4.dp

/**
 * A Paper-styled static card - [PaperListItemCardShape] and elevation matching [ListItemCard],
 * with no click handling or press animation, for grouping arbitrary, non-interactive [content].
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.shadow(elevation = CARD_ELEVATION, shape = PaperListItemCardShape),
        shape = PaperListItemCardShape,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
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
