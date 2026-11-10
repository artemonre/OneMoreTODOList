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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.LocalContentColor
import com.artemonre.onemoretodolist.core.designsystem.components.AppListItemCardContentPadding
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import kotlin.math.sqrt

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
    val foldColor = if (accent) colors.primary else borderColor
    Box(
        modifier = modifier
            .clip(HoloCardShape)
            .background(fill)
            .border(HoloDefaults.BorderWidth, borderColor, HoloCardShape)
            .drawWithContent {
                drawContent()
                val fold = HoloDefaults.CornerFoldSize.toPx()
                // The flap folded over into the cut corner: the triangle under the cut's diagonal.
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
                drawPath(flap, foldColor)
            }
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
 * A static Holo card for grouping non-interactive [content]: rounded, with a thin
 * [MaterialTheme.colorScheme.outlineVariant] border and a highlighted top-end corner - a crescent
 * along the corner's curve, unlike the list cards' folded corner.
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
            .holoCornerHighlight(HoloDefaults.CardCornerRadius)
            .padding(AppSpacing.s)
    ) {
        val onSurface = MaterialTheme.colorScheme.onSurface
        CompositionLocalProvider(LocalContentColor provides contentColorFor(containerColor).takeOrElse { onSurface }) {
            content()
        }
    }
}

/**
 * Draws the static-card crescent (see [cornerHighlight]) over the top-end corner, for a shape
 * whose corners are [cornerRadius].
 */
@Composable
internal fun Modifier.holoCornerHighlight(cornerRadius: Dp): Modifier {
    val highlight = MaterialTheme.colorScheme.primary
    return drawWithContent {
        drawContent()
        val thickness = HoloDefaults.CornerHighlightThickness.toPx()
        drawPath(cornerHighlight(size, cornerRadius.toPx(), thickness, layoutDirection), highlight)
    }
}

/**
 * The crescent along a card's top-end rounded corner: the corner's own curve on the outside, and
 * a flatter curve between the same two ends on the inside - [thickness] apart mid-corner, meeting
 * at both ends.
 */
private fun cornerHighlight(size: Size, radius: Float, thickness: Float, layoutDirection: LayoutDirection): Path {
    // The inner curve is a quadratic through the corner's ends; its control point sits [pull] in
    // from the card's corner along both edges. A quadratic's midpoint is a quarter of each end plus
    // half the control point, and the circular corner's midpoint sits r(1 - 1/sqrt2) in from each
    // edge - solving for the gap between the two along the diagonal gives this.
    val pull = 2f * (thickness / SQRT_2 + radius * (1f - 1f / SQRT_2 - 0.25f))
    return Path().apply {
        if (layoutDirection == LayoutDirection.Ltr) {
            moveTo(size.width - radius, 0f)
            arcTo(Rect(size.width - 2 * radius, 0f, size.width, 2 * radius), -90f, 90f, forceMoveTo = false)
            quadraticTo(size.width - pull, pull, size.width - radius, 0f)
        } else {
            moveTo(radius, 0f)
            arcTo(Rect(0f, 0f, 2 * radius, 2 * radius), -90f, -90f, forceMoveTo = false)
            quadraticTo(pull, pull, radius, 0f)
        }
        close()
    }
}

private val SQRT_2 = sqrt(2f)

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
