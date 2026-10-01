package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// Whether this platform has an on-device "photo -> text" flow at all - callers disable the
// capture affordance where it's false.
expect val isPhotoTextCaptureSupported: Boolean

// A full-screen camera (with a gallery shortcut) that recognizes text on-device and hands back its
// non-blank lines, top to bottom. An empty list means nothing was recognized, whether the photo
// simply had no text or recognition failed. Only shown where isPhotoTextCaptureSupported is true.
@Composable
expect fun PhotoTextCaptureDialog(onTextRecognized: (lines: List<String>) -> Unit, onDismiss: () -> Unit)
