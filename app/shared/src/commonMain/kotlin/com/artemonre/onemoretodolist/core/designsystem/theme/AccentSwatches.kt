package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// A small, fixed set of numbered swatches for user-chosen accents (e.g. tags) that sit outside the
// Material color scheme's semantic roles. Each is a container/content pair so text drawn on it
// always keeps contrast, with separate light and dark sets picked by AppTheme alongside the color
// scheme. Domain-agnostic - features map their own concepts onto the slots by index.
data class AccentSwatch(val container: Color, val content: Color)

data class AccentSwatches(val slots: List<AccentSwatch>)

// Every color is borrowed from one of the app's palettes (Color.kt), so accents stay in the app's
// color language. Light mode takes the palettes' strong colors with white text; dark mode their
// light ones with dark text. Slot N keeps the same hue family in both. Each container stands out
// from every palette's cards, with readable text on it - AccentSwatchesContrastTest checks both,
// so retuning a palette can't quietly break a tag.
private val OnStrongSwatch = LightColorSchemeDefault.onPrimary
private val OnLightSwatch = LightColorSchemeDefault.onSurface

val LightAccentSwatches = AccentSwatches(
    listOf(
        LightColorSchemeDefault.errorContainer, // red
        MaterialPaletteYellowLightColorScheme.primaryContainer, // gold
        MaterialPaletteGreenLightColorScheme.primaryContainer, // olive
        LightColorSchemeDefault.primary, // teal
        DarkColorSchemeDefault.inversePrimary, // petrol
        LightColorSchemeDefault.tertiaryContainer, // indigo
        SlateDarkColorScheme.onTertiary, // plum
        SlateLightColorScheme.primary // steel blue
    ).map { AccentSwatch(it, OnStrongSwatch) }
)

val DarkAccentSwatches = AccentSwatches(
    listOf(
        DarkColorSchemeDefault.errorContainer, // red
        MaterialPaletteYellowDarkColorScheme.primary, // gold
        MaterialPaletteGreenDarkColorScheme.primary, // lime
        LightColorSchemeDefault.primaryContainer, // green
        DarkColorSchemeDefault.primary, // cyan
        DarkColorSchemeDefault.tertiaryContainer, // periwinkle
        SlateDarkColorScheme.tertiary, // lavender
        LightColorSchemeDefault.surfaceContainer // sky blue
    ).map { AccentSwatch(it, OnLightSwatch) }
)

val LocalAccentSwatches = staticCompositionLocalOf { LightAccentSwatches }
