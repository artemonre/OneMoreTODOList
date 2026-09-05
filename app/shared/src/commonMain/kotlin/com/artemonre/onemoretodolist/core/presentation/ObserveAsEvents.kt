package com.artemonre.onemoretodolist.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.flow.Flow

@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val latestOnEvent by rememberUpdatedState(onEvent)
    LaunchedEffect(flow) {
        flow.collect { latestOnEvent(it) }
    }
}
