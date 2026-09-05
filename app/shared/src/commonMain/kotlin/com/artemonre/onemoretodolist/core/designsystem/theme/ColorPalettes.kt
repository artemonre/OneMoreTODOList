package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption

data class ColorPalette(
    val light: ColorScheme,
    val dark: ColorScheme
)

private val DefaultPalette = ColorPalette(light = LightColorSchemeDefault, dark = DarkColorSchemeDefault)
private val SlatePalette = ColorPalette(light = SlateLightColorScheme, dark = SlateDarkColorScheme)

fun ColorPaletteOption.toColorPalette(): ColorPalette = when (this) {
    ColorPaletteOption.Default -> DefaultPalette
    ColorPaletteOption.Slate -> SlatePalette
}
