package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@ExperimentalComposeUiApi
expect fun createPlainTextClipEntry(text: String): ClipEntry

// True where the OS already shows its own copy confirmation (Android 13+'s clipboard toast).
expect val hasNativeCopyConfirmation: Boolean
