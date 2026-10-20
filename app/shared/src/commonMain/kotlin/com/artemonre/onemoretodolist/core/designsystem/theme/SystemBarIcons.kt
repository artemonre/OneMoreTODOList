package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.runtime.Composable

// Points the status/navigation bar icons of the window hosting the calling composable at the
// app's own theme - dark icons when [darkIcons], light otherwise. enableEdgeToEdge()'s defaults
// follow the *system* dark mode, so without this an app-level Dark theme on a light-mode phone
// ends up with dark icons on a dark background. Works for both an Activity's window and a
// Dialog's own window. Android-only concern, hence expect/actual (no-op elsewhere).
@Composable
internal expect fun ConfigureSystemBarIcons(darkIcons: Boolean)
