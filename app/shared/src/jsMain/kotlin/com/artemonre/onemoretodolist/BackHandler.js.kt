package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No system back gesture/button to intercept on web (JS).
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}
