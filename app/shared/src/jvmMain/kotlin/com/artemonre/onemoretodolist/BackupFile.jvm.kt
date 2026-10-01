package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.backup_export_dialog_title
import onemoretodolist.app.shared.generated.resources.backup_import_dialog_title
import org.jetbrains.compose.resources.stringResource

// AWT's FileDialog rather than Swing's JFileChooser - it's the OS's own native dialog on
// Windows/macOS. It's modal, so showing it blocks until the user picks or cancels; only the file IO
// itself moves off the UI thread.
@Composable
actual fun rememberBackupFileExporter(
    onFinished: (saved: Boolean) -> Unit
): ((suggestedName: String, json: String) -> Unit)? {
    val coroutineScope = rememberCoroutineScope()
    val currentOnFinished by rememberUpdatedState(onFinished)
    val title = stringResource(Res.string.backup_export_dialog_title)
    return remember(title) {
        { suggestedName, json ->
            val dialog = FileDialog(null as Frame?, title, FileDialog.SAVE).apply {
                file = suggestedName
                isVisible = true
            }
            val target = dialog.file?.let { File(dialog.directory, it) }
            if (target != null) {
                coroutineScope.launch {
                    val saved = withContext(Dispatchers.IO) {
                        try {
                            target.writeText(json)
                            true
                        } catch (e: IOException) {
                            false
                        }
                    }
                    currentOnFinished(saved)
                }
            }
        }
    }
}

@Composable
actual fun rememberBackupFileImporter(onJsonRead: (json: String?) -> Unit): (() -> Unit)? {
    val coroutineScope = rememberCoroutineScope()
    val currentOnJsonRead by rememberUpdatedState(onJsonRead)
    val title = stringResource(Res.string.backup_import_dialog_title)
    return remember(title) {
        {
            val dialog = FileDialog(null as Frame?, title, FileDialog.LOAD).apply {
                setFilenameFilter { _, name -> name.endsWith(".json", ignoreCase = true) }
                isVisible = true
            }
            val source = dialog.file?.let { File(dialog.directory, it) }
            if (source != null) {
                coroutineScope.launch {
                    val json = withContext(Dispatchers.IO) {
                        try {
                            source.readText()
                        } catch (e: IOException) {
                            null
                        }
                    }
                    currentOnJsonRead(json)
                }
            }
        }
    }
}
