package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

// No system back gesture/button to intercept on web (JS).
@Composable
actual fun PredictiveBackHandler(enabled: Boolean, onBack: suspend (progress: Flow<Float>) -> Unit) {
}
