package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// Desktop has no in-app update flow - there's no store to check against.
actual suspend fun isAppUpdateAvailable(): Boolean = false

actual suspend fun isAppUpdateMandatory(): Boolean = false

@Composable
actual fun rememberAppUpdateLauncher(): (() -> Unit)? = null

@Composable
actual fun MandatoryAppUpdateGate(content: @Composable () -> Unit) = content()
