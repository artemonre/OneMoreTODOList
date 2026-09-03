package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// A cross-platform stand-in for androidx.activity.compose.BackHandler, which is Android-only.
// Intercepts the system back gesture/button while [enabled], invoking [onBack] instead. A no-op
// on platforms without a system back concept.
@Composable
expect fun BackHandler(enabled: Boolean, onBack: () -> Unit)
