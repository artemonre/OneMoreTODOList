package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.LocalContentColor
import com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCardContentPadding
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A Holo list row: an outlined card with a folded top-end corner. The fold is drawn in
 * [MaterialTheme.colorScheme.primary] when [accent] is on (callers use it to mark a row, e.g. a
 * todo with a reminder) and in the border color otherwise. Domain-agnostic, like the other styles'
 * list cards.
 */
@Composable
fun HoloListItemCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    accent: Boolean = false,
    customActions: List<CustomAccessibilityAction> = emptyList(),
    content: @Composable () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val fill = containerColor ?: colors.surface
    val borderColor = colors.outlineVariant
    val foldColor = holoCornerFlapColor(accent)
    Box(
        modifier = modifier
            .clip(HoloCardShape)
            .background(fill)
            .border(HoloDefaults.BorderWidth, borderColor, HoloCardShape)
            .holoCornerFlap(foldColor)
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .semantics { this.customActions = customActions }
            .padding(AppListItemCardContentPadding)
    ) {
        // contentColorFor has no match for colors outside the scheme - fall back to onSurface.
        CompositionLocalProvider(LocalContentColor provides contentColorFor(fill).takeOrElse { colors.onSurface }) {
            content()
        }
    }
}

/**
 * The color of a folded corner's flap: [MaterialTheme.colorScheme.primary] when [accent] is on,
 * the cards' border color otherwise.
 */
@Composable
internal fun holoCornerFlapColor(accent: Boolean): Color =
    if (accent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

/**
 * Draws the flap folded over into the cut corner of a [HoloFoldedCornerShape] - the triangle
 * under the cut's diagonal - on top of the content.
 */
internal fun Modifier.holoCornerFlap(color: Color): Modifier = drawWithContent {
    drawContent()
    val fold = HoloDefaults.CornerFoldSize.toPx()
    val flap = Path().apply {
        if (layoutDirection == LayoutDirection.Ltr) {
            moveTo(size.width - fold, 0f)
            lineTo(size.width - fold, fold)
            lineTo(size.width, fold)
        } else {
            moveTo(fold, 0f)
            lineTo(fold, fold)
            lineTo(0f, fold)
        }
        close()
    }
    drawPath(flap, color)
}

/**
 * A static Holo card for grouping non-interactive [content]: rounded, with a thin
 * [MaterialTheme.colorScheme.outlineVariant] border.
 */
@Composable
fun HoloCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(HoloDefaults.CardCornerRadius)
    Box(
        modifier = modifier
            .clip(shape)
            .background(containerColor)
            .border(HoloDefaults.BorderWidth, MaterialTheme.colorScheme.outlineVariant, shape)
            .padding(AppSpacing.s)
    ) {
        val onSurface = MaterialTheme.colorScheme.onSurface
        CompositionLocalProvider(LocalContentColor provides contentColorFor(containerColor).takeOrElse { onSurface }) {
            content()
        }
    }
}

@Preview(widthDp = 280, heightDp = 160)
@Composable
private fun HoloListItemCardPreview() {
    AppTheme(themeConfig = ThemeConfig(uiStyle = UiStyleOption.Holo)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s), modifier = Modifier.padding(AppSpacing.s)) {
            HoloListItemCard(onClick = {}, accent = true, modifier = Modifier.fillMaxWidth()) { Text("Pay your taxes") }
            HoloListItemCard(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Buy groceries") }
        }
    }
}

@Preview(widthDp = 280, heightDp = 160)
@Composable
private fun HoloListItemCardDarkPreview() {
    AppTheme(themeConfig = ThemeConfig(mode = ThemeMode.Dark, uiStyle = UiStyleOption.Holo)) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.s), modifier = Modifier.padding(AppSpacing.s)) {
            HoloListItemCard(onClick = {}, accent = true, modifier = Modifier.fillMaxWidth()) { Text("Pay your taxes") }
            HoloCard(modifier = Modifier.fillMaxWidth()) { Text("Section content") }
        }
    }
}
