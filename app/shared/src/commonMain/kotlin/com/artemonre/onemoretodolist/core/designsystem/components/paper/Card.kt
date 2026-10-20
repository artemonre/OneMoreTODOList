package com.artemonre.onemoretodolist.core.designsystem.components.paper

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Same elevation as ListItemCard, minus its press-driven flatten - nothing to press on a static
// container.
private val CARD_ELEVATION = 6.dp

/**
 * A Paper-styled static card - [PaperListItemCardShape] and elevation matching [ListItemCard],
 * with no click handling or press animation, for grouping arbitrary, non-interactive [content].
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    containerColor: Color = CardDefaults.elevatedCardColors().containerColor,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier.shadow(elevation = CARD_ELEVATION, shape = PaperListItemCardShape),
        shape = PaperListItemCardShape,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.padding(AppSpacing.s)) {
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
