package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared measurements for the Holo style. Holo has no colors of its own - every color comes from
 * the current [MaterialTheme.colorScheme], so it follows the palette, dark mode and dynamic color.
 */
internal object HoloDefaults {
    val CardCornerRadius = 16.dp
    val HeroCardCornerRadius = 24.dp
    val TileCornerRadius = 12.dp
    val ControlCornerRadius = 12.dp
    val CheckboxCornerRadius = 4.dp
    val FabCornerRadius = 16.dp
    val BorderWidth = 2.dp
    val ThinBorderWidth = 1.dp
    val CornerFoldSize = 12.dp
    // How thick the static card's corner highlight is mid-corner - see HoloCard.
    val CornerHighlightThickness = 6.dp
    val FabGlowElevation = 12.dp
    val HeroAccentBarWidth = 48.dp
    val HeroAccentBarThickness = 4.dp
    val LabelLetterSpacing = 1.sp

    // Decorative glyphs, not text: TalkBack never reads them (see the components using them).
    const val PROMPT_GLYPH = ">"
    const val TAG_GLYPH = "#"
    const val SECTION_SEPARATOR = "//"

    // Low alphas for the frosted backgrounds and soft borders.
    const val FROST_ALPHA = 0.5f
    const val SOFT_BORDER_ALPHA = 0.4f
    const val TILE_ALPHA = 0.8f
}

/**
 * A rounded rectangle with its top-end corner cut off diagonally - the dog-ear of a Holo list
 * card. [HoloListItemCard] draws the folded-over flap into the cut.
 */
internal class HoloFoldedCornerShape(
    private val cornerRadius: Dp,
    private val foldSize: Dp
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { cornerRadius.toPx() }
        val fold = with(density) { foldSize.toPx() }
        val w = size.width
        val h = size.height
        val path = Path().apply {
            if (layoutDirection == LayoutDirection.Ltr) {
                moveTo(r, 0f)
                lineTo(w - fold, 0f)
                lineTo(w, fold)
                lineTo(w, h - r)
                quadraticTo(w, h, w - r, h)
                lineTo(r, h)
                quadraticTo(0f, h, 0f, h - r)
                lineTo(0f, r)
                quadraticTo(0f, 0f, r, 0f)
            } else {
                moveTo(fold, 0f)
                lineTo(w - r, 0f)
                quadraticTo(w, 0f, w, r)
                lineTo(w, h - r)
                quadraticTo(w, h, w - r, h)
                lineTo(r, h)
                quadraticTo(0f, h, 0f, h - r)
                lineTo(0f, fold)
            }
            close()
        }
        return Outline.Generic(path)
    }
}

internal val HoloCardShape: Shape = HoloFoldedCornerShape(HoloDefaults.CardCornerRadius, HoloDefaults.CornerFoldSize)

// Material3's opacity for disabled content.
internal const val HOLO_DISABLED_ALPHA = 0.38f

internal val HoloControlShape = RoundedCornerShape(HoloDefaults.ControlCornerRadius)

/**
 * The Holo look shared by chips and the tonal button: a rounded rectangle, filled with a darker
 * outline when [selected], frosted with a [MaterialTheme.colorScheme.primary] outline otherwise.
 * Clips to the shape, so a click ripple added after it stays inside.
 */
@Composable
internal fun Modifier.holoControl(selected: Boolean): Modifier {
    val colors = MaterialTheme.colorScheme
    val fill = if (selected) colors.secondaryContainer else colors.surface.copy(alpha = HoloDefaults.FROST_ALPHA)
    val border = if (selected) colors.outline else colors.primary
    return this
        .clip(HoloControlShape)
        .background(fill, HoloControlShape)
        .border(HoloDefaults.BorderWidth, border, HoloControlShape)
}

@Composable
internal fun holoControlContentColor(selected: Boolean): Color =
    if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface

internal val HoloTileShape = RoundedCornerShape(HoloDefaults.TileCornerRadius)
