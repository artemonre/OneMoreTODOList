package com.artemonre.onemoretodolist

import androidx.activity.compose.PredictiveBackHandler as ActivityPredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
actual fun PredictiveBackHandler(enabled: Boolean, onBack: suspend (progress: Flow<Float>) -> Unit) {
    ActivityPredictiveBackHandler(enabled = enabled) { events -> onBack(events.map { it.progress }) }
}
