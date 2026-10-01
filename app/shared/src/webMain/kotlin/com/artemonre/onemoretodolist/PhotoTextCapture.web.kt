package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// No on-device text recognition wired up here yet - the capture affordance stays disabled.
actual val isPhotoTextCaptureSupported: Boolean = false

@Composable
actual fun PhotoTextCaptureDialog(onTextRecognized: (lines: List<String>) -> Unit, onDismiss: () -> Unit) = Unit
