package com.artemonre.onemoretodolist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

// Prefers genuine on-device recognition (guaranteed local processing, no network usage) and
// falls back to the platform's default recognition service - which may itself be network-backed,
// offline, or unavailable depending on the device - only when no on-device engine exists.
// isOnDeviceRecognitionAvailable/createOnDeviceSpeechRecognizer require API 31+; on-device is
// simply treated as unavailable below that.
@Composable
internal actual fun rememberPlatformSpeechToText(onResult: (text: String) -> Unit): SpeechToTextController? {
    val context = LocalContext.current
    val onDeviceAvailable = remember(context) {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    }
    val networkAvailable = remember(context) {
        SpeechRecognizer.isRecognitionAvailable(context)
    }
    if (!onDeviceAvailable && !networkAvailable) return null

    val currentOnResult = rememberUpdatedState(onResult)
    val stateHolder = remember { mutableStateOf<SpeechRecognitionState>(SpeechRecognitionState.Idle) }

    val recognizer = remember(context) {
        val speechRecognizer = if (onDeviceAvailable) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }
        speechRecognizer.apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    stateHolder.value = SpeechRecognitionState.Listening
                }

                override fun onResults(results: Bundle?) {
                    val transcript = results
                        ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()
                    stateHolder.value = SpeechRecognitionState.Idle
                    if (!transcript.isNullOrBlank()) currentOnResult.value(transcript)
                }

                override fun onError(error: Int) {
                    stateHolder.value = SpeechRecognitionState.Error(error.toDescription())
                }

                override fun onBeginningOfSpeech() = Unit
                override fun onRmsChanged(rmsdB: Float) = Unit
                override fun onBufferReceived(buffer: ByteArray?) = Unit
                override fun onEndOfSpeech() = Unit
                override fun onPartialResults(partialResults: Bundle?) = Unit
                override fun onEvent(eventType: Int, params: Bundle?) = Unit
            })
        }
    }

    DisposableEffect(recognizer) {
        onDispose { recognizer.destroy() }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            recognizer.startListening(recognitionIntent())
        } else {
            stateHolder.value = SpeechRecognitionState.Error("Microphone permission denied")
        }
    }

    return remember(recognizer) {
        object : SpeechToTextController {
            override val state: State<SpeechRecognitionState> = stateHolder

            override fun start() {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) {
                    recognizer.startListening(recognitionIntent())
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }

            override fun stop() {
                recognizer.stopListening()
            }
        }
    }
}

private fun recognitionIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    }

private fun Int.toDescription(): String = when (this) {
    SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that"
    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission denied"
    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
    SpeechRecognizer.ERROR_NETWORK -> "Network error"
    else -> "Speech recognition error"
}
