package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material3.ColorScheme

// Dynamic color (Material You, wallpaper-derived) only exists on Android 12+ (API 31,
// Build.VERSION_CODES.S) - the OS version that introduced per-user color extraction. Every other
// platform, and older Android, doesn't have it at all - see the per-platform actuals.
expect fun isDynamicColorSupported(): Boolean

// The dynamically-extracted color scheme, or null wherever isDynamicColorSupported() is false -
// callers fall back to a palette-based ColorScheme in that case.
expect fun dynamicColorScheme(useDarkTheme: Boolean): ColorScheme?
