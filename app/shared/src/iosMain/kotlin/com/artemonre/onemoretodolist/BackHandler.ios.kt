package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No system back gesture/button to intercept on iOS.
@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
}
