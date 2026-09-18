package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// True when the platform's store has a newer version ready to install - always false where no
// such update flow exists (every build outside the Play-distributed Android app).
expect suspend fun isAppUpdateAvailable(): Boolean

// True when the available update must be installed before the app can be used - always false
// where no such update flow exists.
expect suspend fun isAppUpdateMandatory(): Boolean

// Returns a launcher that starts the platform's update flow, or null where none exists yet -
// callers should just hide their update button in that case.
@Composable
expect fun rememberAppUpdateLauncher(): (() -> Unit)?

// Wraps the app content, blocking it behind a forced update screen when isAppUpdateMandatory()
// is true - a no-op passthrough on every platform without a mandatory update flow.
@Composable
expect fun MandatoryAppUpdateGate(content: @Composable () -> Unit)
