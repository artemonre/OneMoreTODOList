package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption

data class ColorPalette(
    val light: ColorScheme,
    val dark: ColorScheme
)

private val IndigoPalette = ColorPalette(light = LightColorScheme, dark = DarkColorScheme)
private val SlatePalette = ColorPalette(light = SlateLightColorScheme, dark = SlateDarkColorScheme)

fun ColorPaletteOption.toColorPalette(): ColorPalette = when (this) {
    ColorPaletteOption.Indigo -> IndigoPalette
    ColorPaletteOption.Slate -> SlatePalette
}
