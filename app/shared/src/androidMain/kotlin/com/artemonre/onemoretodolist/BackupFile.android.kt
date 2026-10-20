package com.artemonre.onemoretodolist

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.io.FileNotFoundException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BACKUP_MIME_TYPE = "application/json"

// File managers disagree on what a .json file is - some report text/plain or a generic binary type.
private val IMPORT_MIME_TYPES = arrayOf(BACKUP_MIME_TYPE, "text/*", "application/octet-stream")

// The system document UI (Storage Access Framework) - no storage permission, and the user can pick
// Drive, Downloads, a USB stick, etc.
@Composable
actual fun rememberBackupFileExporter(
    onFinished: (saved: Boolean) -> Unit
): ((suggestedName: String, json: String) -> Unit)? {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnFinished by rememberUpdatedState(onFinished)
    // Held only while the save dialog is open - not saved state, so a process death in between
    // drops the export rather than writing a stale snapshot.
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE)
    ) { uri: Uri? ->
        val json = pendingJson
        pendingJson = null
        if (uri == null || json == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val saved = withContext(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openOutputStream(uri, "wt")
                        ?: throw FileNotFoundException("Couldn't open $uri")
                    stream.bufferedWriter().use { it.write(json) }
                    true
                } catch (e: IOException) {
                    false
                }
            }
            currentOnFinished(saved)
        }
    }
    return remember(launcher) {
        { suggestedName, json ->
            pendingJson = json
            launcher.launch(suggestedName)
        }
    }
}

@Composable
actual fun rememberBackupFileImporter(onJsonRead: (json: String?) -> Unit): (() -> Unit)? {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentOnJsonRead by rememberUpdatedState(onJsonRead)

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        coroutineScope.launch {
            val json = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                } catch (e: IOException) {
                    null
                }
            }
            currentOnJsonRead(json)
        }
    }
    return remember(launcher) { { launcher.launch(IMPORT_MIME_TYPES) } }
}
