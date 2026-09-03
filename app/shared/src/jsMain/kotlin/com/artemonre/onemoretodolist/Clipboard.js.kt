package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.ClipboardItem

@OptIn(ExperimentalComposeUiApi::class)
actual fun createPlainTextClipEntry(text: String): ClipEntry {
    val item = js(
        "new ClipboardItem({ 'text/plain': new Blob([text], { type: 'text/plain' }) })"
    ).unsafeCast<ClipboardItem>()
    return ClipEntry(arrayOf(item))
}

actual val hasNativeCopyConfirmation: Boolean = false
