package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No offline on-device implementation yet - callers hide the mic affordance.
@Composable
internal actual fun rememberPlatformSpeechToText(onResult: (text: String) -> Unit): SpeechToTextController? = null
