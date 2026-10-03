package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.artemonre.onemoretodolist.core.designsystem.components.material.Card as MaterialCard
import com.artemonre.onemoretodolist.core.designsystem.components.paper.Card as PaperCard
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * Renders a static, non-interactive card for the current [LocalUiStyle], falling back to the
 * plain Material3 card for any style without its own implementation. Domain-agnostic: any feature
 * can use this to group related content into a card without this module knowing what that content
 * represents.
 */
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    containerColor: Color = CardDefaults.elevatedCardColors().containerColor,
    content: @Composable () -> Unit
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialCard(modifier, containerColor, content)
        UiStyleOption.Paper -> PaperCard(modifier, containerColor, content)
    }
}
