package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import com.artemonre.onemoretodolist.core.designsystem.components.paper.PaperListItemCardShape
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * The corner [Shape] of [AppListItemCard] for the current [LocalUiStyle] - kept in sync with it
 * so callers that draw behind/around the card (e.g. a swipe-reveal background) can match its
 * corners exactly instead of guessing a fixed radius.
 */
@Composable
fun appListItemCardShape(): Shape = when (LocalUiStyle.current) {
    // The Material card is an ElevatedCard - match its shape, not the plain Card's.
    UiStyleOption.Material -> CardDefaults.elevatedShape
    // Falls back to the Material card - match its shape too.
    UiStyleOption.Neumorphic -> CardDefaults.elevatedShape
    UiStyleOption.Paper -> PaperListItemCardShape
}
