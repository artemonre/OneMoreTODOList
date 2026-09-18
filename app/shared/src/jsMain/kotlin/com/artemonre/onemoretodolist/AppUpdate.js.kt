package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No in-app update flow on web (JS) - a page reload always serves the latest version.
actual suspend fun isAppUpdateAvailable(): Boolean = false

actual suspend fun isAppUpdateMandatory(): Boolean = false

@Composable
actual fun rememberAppUpdateLauncher(): (() -> Unit)? = null

@Composable
actual fun MandatoryAppUpdateGate(content: @Composable () -> Unit) = content()
