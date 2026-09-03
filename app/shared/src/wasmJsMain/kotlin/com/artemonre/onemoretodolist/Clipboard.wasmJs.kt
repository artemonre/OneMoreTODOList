package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipboardItem
import kotlin.js.JsArray

@OptIn(ExperimentalComposeUiApi::class)
private fun createClipboardItems(text: String): JsArray<ClipboardItem> =
    js("[new ClipboardItem({ 'text/plain': new Blob([text], { type: 'text/plain' }) })]")

@OptIn(ExperimentalComposeUiApi::class)
actual fun createPlainTextClipEntry(text: String): ClipEntry =
    ClipEntry(createClipboardItems(text))

actual val hasNativeCopyConfirmation: Boolean = false
