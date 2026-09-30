package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// A small, fixed set of hue swatches for user-chosen accents (e.g. tags) that sit outside the
// Material color scheme's semantic roles. Each is a container/content pair so text drawn on it
// always keeps contrast, with separate light and dark variants picked by AppTheme alongside the
// color scheme. Domain-agnostic - features map their own concepts onto these slots.
data class AccentSwatch(val container: Color, val content: Color)

data class AccentSwatches(
    val red: AccentSwatch,
    val orange: AccentSwatch,
    val yellow: AccentSwatch,
    val green: AccentSwatch,
    val teal: AccentSwatch,
    val blue: AccentSwatch,
    val purple: AccentSwatch,
    val pink: AccentSwatch
)

val LightAccentSwatches = AccentSwatches(
    red = AccentSwatch(Color(0xFFFFDAD6), Color(0xFF410002)),
    orange = AccentSwatch(Color(0xFFFFDCC1), Color(0xFF2E1500)),
    yellow = AccentSwatch(Color(0xFFFFE08A), Color(0xFF241A00)),
    green = AccentSwatch(Color(0xFFC8EFC0), Color(0xFF002204)),
    teal = AccentSwatch(Color(0xFFB3EFE4), Color(0xFF00201C)),
    blue = AccentSwatch(Color(0xFFD3E3FF), Color(0xFF001B3D)),
    purple = AccentSwatch(Color(0xFFEBDCFF), Color(0xFF25005A)),
    pink = AccentSwatch(Color(0xFFFFD8E8), Color(0xFF3E0021))
)

val DarkAccentSwatches = AccentSwatches(
    red = AccentSwatch(Color(0xFF93000A), Color(0xFFFFDAD6)),
    orange = AccentSwatch(Color(0xFF6B3B00), Color(0xFFFFDCC1)),
    yellow = AccentSwatch(Color(0xFF564500), Color(0xFFFFE08A)),
    green = AccentSwatch(Color(0xFF1E5127), Color(0xFFC8EFC0)),
    teal = AccentSwatch(Color(0xFF00504A), Color(0xFFB3EFE4)),
    blue = AccentSwatch(Color(0xFF004A8F), Color(0xFFD3E3FF)),
    purple = AccentSwatch(Color(0xFF5A3D8E), Color(0xFFEBDCFF)),
    pink = AccentSwatch(Color(0xFF8E2B5A), Color(0xFFFFD8E8))
)

val LocalAccentSwatches = staticCompositionLocalOf { LightAccentSwatches }
