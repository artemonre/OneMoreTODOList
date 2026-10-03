package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

/**
 * A stock Material3 [ElevatedCard] with no click handling - a static container for arbitrary
 * [content]. Domain-agnostic: any feature can use it to group related content into a card without
 * this module knowing what that content represents.
 */
@Composable
fun Card(
    modifier: Modifier = Modifier,
    containerColor: Color = CardDefaults.elevatedCardColors().containerColor,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(containerColor = containerColor)
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
