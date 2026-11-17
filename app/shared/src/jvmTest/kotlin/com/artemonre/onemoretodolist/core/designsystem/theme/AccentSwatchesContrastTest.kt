package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertTrue

// The accent swatches borrow their colors from the palettes, but every palette's cards sit behind
// them. Each swatch must keep its text readable and stand out from the cards of every palette, in
// light and dark - so retuning a palette color can't quietly break a tag.
class AccentSwatchesContrastTest {

    @Test
    fun `text on every swatch is readable`() {
        val failures = swatchesByMode().flatMap { (mode, swatches) ->
            swatches.slots.mapIndexedNotNull { index, swatch ->
                val ratio = contrast(swatch.container, swatch.content)
                "$mode slot ${index + 1}: text ${ratio.format()}".takeIf { ratio < MIN_TEXT_CONTRAST }
            }
        }
        assertTrue(failures.isEmpty(), "Text contrast below $MIN_TEXT_CONTRAST:1 - $failures")
    }

    @Test
    fun `every swatch stands out from every palette's cards`() {
        val failures = ColorPaletteOption.entries.flatMap { option ->
            val palette = option.toColorPalette()
            listOf("light" to palette.light, "dark" to palette.dark).flatMap { (mode, scheme) ->
                val swatches = if (mode == "light") LightAccentSwatches else DarkAccentSwatches
                swatches.slots.mapIndexedNotNull { index, swatch ->
                    val ratio = scheme.cardColors().minOf { contrast(swatch.container, it) }
                    "$option $mode slot ${index + 1}: ${ratio.format()}".takeIf { ratio < MIN_CARD_CONTRAST }
                }
            }
        }
        assertTrue(failures.isEmpty(), "Contrast against cards below $MIN_CARD_CONTRAST:1 - $failures")
    }

    private fun swatchesByMode() = listOf("light" to LightAccentSwatches, "dark" to DarkAccentSwatches)

    // Cards are drawn on surfaceContainerLow (Material's elevated card) or surface (Holo).
    private fun ColorScheme.cardColors() = listOf(surfaceContainerLow, surface)

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (max(la, lb) + 0.05f) / (min(la, lb) + 0.05f)
    }

    private fun Float.format() = "${(this * 100).toInt() / 100f}:1"

    private companion object {
        // WCAG AA: 4.5:1 for normal-size text, 3:1 for non-text shapes like a tag's fill.
        const val MIN_TEXT_CONTRAST = 4.5f
        const val MIN_CARD_CONTRAST = 3f
    }
}
