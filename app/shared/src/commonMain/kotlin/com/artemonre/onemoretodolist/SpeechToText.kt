package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State

sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data object Listening : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}

interface SpeechToTextController {
    val state: State<SpeechRecognitionState>
    fun start()
    fun stop()
}

// Returns a controller for on-device speech-to-text, or null where no offline engine is
// available (unsupported platform, or this device has none) - callers should hide the mic
// affordance when this is null. Capitalizes the transcript's first letter so every caller
// gets consistent formatting without repeating it.
@Composable
fun rememberSpeechToText(onResult: (text: String) -> Unit): SpeechToTextController? =
    rememberPlatformSpeechToText { onResult(it.replaceFirstChar(Char::titlecase)) }

@Composable
internal expect fun rememberPlatformSpeechToText(onResult: (text: String) -> Unit): SpeechToTextController?
