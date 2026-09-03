package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No native share sheet wired up on iOS yet - callers fall back to the clipboard.
@Composable
actual fun rememberNativeShareLauncher(): ((String) -> Unit)? = null
