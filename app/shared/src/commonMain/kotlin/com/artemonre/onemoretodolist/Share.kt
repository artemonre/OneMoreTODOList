package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// Returns a launcher for the platform's native share sheet, or null where none exists yet -
// callers should fall back to copying the text to the clipboard instead.
@Composable
expect fun rememberNativeShareLauncher(): ((text: String) -> Unit)?
