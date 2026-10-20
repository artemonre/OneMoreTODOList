package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable

// Saving/opening a backup file through the platform's own file UI (system document picker, native
// file dialog, browser download/upload). Both return null where there's no such UI - callers hide
// the matching button then.

// The returned launcher asks where to save `json` (suggesting suggestedName) and writes it.
// onFinished reports whether the file was written; it isn't called at all if the user cancels.
@Composable
expect fun rememberBackupFileExporter(
    onFinished: (saved: Boolean) -> Unit
): ((suggestedName: String, json: String) -> Unit)?

// The returned launcher lets the user pick a file and hands back its text - null if it couldn't be
// read. Not called at all if the user cancels.
@Composable
expect fun rememberBackupFileImporter(onJsonRead: (json: String?) -> Unit): (() -> Unit)?
