package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.todolist.presentation.ScannedParagraph

// No on-device text recognition wired up here yet - the capture affordance stays disabled.
actual val isPhotoTextCaptureSupported: Boolean = false

@Composable
actual fun PhotoTextCaptureDialog(onTextRecognized: (paragraphs: List<ScannedParagraph>) -> Unit, onDismiss: () -> Unit) = Unit
