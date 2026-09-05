package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// Desktop has no OS-level share sheet - callers fall back to the clipboard.
@Composable
actual fun rememberNativeShareLauncher(): ((String) -> Unit)? = null
