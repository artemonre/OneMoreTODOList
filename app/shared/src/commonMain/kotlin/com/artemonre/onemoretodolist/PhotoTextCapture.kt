package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.feature.todolist.presentation.ScannedParagraph

// Whether this platform has an on-device "photo -> text" flow at all - callers disable the
// capture affordance where it's false.
expect val isPhotoTextCaptureSupported: Boolean

// A full-screen camera (with a gallery shortcut) that recognizes text on-device and hands back its
// paragraphs, top to bottom - wrapped lines already joined, each still carrying its lines. An empty
// list means the photo couldn't be loaded - finding no text is handled inside the dialog (Android
// asks to reframe or retake), and the caller's snackbar says the photo couldn't be opened. Only shown
// where isPhotoTextCaptureSupported is true.
@Composable
expect fun PhotoTextCaptureDialog(onTextRecognized: (paragraphs: List<ScannedParagraph>) -> Unit, onDismiss: () -> Unit)
