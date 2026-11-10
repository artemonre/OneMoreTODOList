package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.artemonre.onemoretodolist.core.designsystem.components.material.Card as MaterialCard
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloCard
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
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
        UiStyleOption.Holo -> HoloCard(modifier, containerColor, content)
    }
}

/**
 * A bordered, unfilled section for the current [LocalUiStyle] - Material3's [OutlinedCard] by
 * default. For a block of settings inside a form, where a filled [AppCard] would be too heavy.
 * [footer], when set, is the card's bottom strip for notes about the block: edge to edge on the
 * screen's background color, so it reads as part of the card rather than as a control. It
 * resizes smoothly as its text changes.
 */
@Composable
fun AppOutlinedCard(
    modifier: Modifier = Modifier,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    when (LocalUiStyle.current) {
        // No Holo outlined card yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> OutlinedCard(modifier = modifier) {
            content()
            if (footer != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .animateContentSize()
                        .padding(AppSpacing.m),
                    content = footer
                )
            }
        }
    }
}
