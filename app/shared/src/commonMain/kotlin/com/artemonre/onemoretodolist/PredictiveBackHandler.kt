package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

// A cross-platform stand-in for androidx.activity.compose.PredictiveBackHandler, which is
// Android-only. onBack receives a Flow of progress values (0f..1f) for a gesture in progress,
// or an immediately-completing empty Flow for a plain back-button press.
@Composable
expect fun PredictiveBackHandler(enabled: Boolean, onBack: suspend (progress: Flow<Float>) -> Unit)
