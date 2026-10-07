package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption

data class ColorPalette(
    val light: ColorScheme,
    val dark: ColorScheme
)

private val DefaultPalette = ColorPalette(light = LightColorSchemeDefault, dark = DarkColorSchemeDefault)
private val SlatePalette = ColorPalette(light = SlateLightColorScheme, dark = SlateDarkColorScheme)
private val MaterialPaletteGreenPalette = ColorPalette(
    light = MaterialPaletteGreenLightColorScheme,
    dark = MaterialPaletteGreenDarkColorScheme
)

// The palette the current colors come from, or null when dynamic color is overriding it.
val LocalColorPalette = staticCompositionLocalOf<ColorPaletteOption?> { ColorPaletteOption.Default }

fun ColorPaletteOption.toColorPalette(): ColorPalette = when (this) {
    ColorPaletteOption.Default -> DefaultPalette
    ColorPaletteOption.Slate -> SlatePalette
    ColorPaletteOption.MaterialPaletteGreen -> MaterialPaletteGreenPalette
}
