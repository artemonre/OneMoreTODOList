package com.artemonre.onemoretodolist.feature.backup.presentation

import androidx.compose.runtime.State
import kotlin.time.Instant

// The daily automatic backup to a file the user picks once. Android-only for now - see
// rememberFileAutoBackup.
interface FileAutoBackupController {
    val state: State<FileAutoBackupState>

    // true asks the user where to save (system file picker) and starts from there.
    fun setEnabled(enabled: Boolean)
}

data class FileAutoBackupState(
    val isEnabled: Boolean = false,
    val isBusy: Boolean = false,
    val lastBackupAt: Instant? = null,
    // The chosen file can't be written any more - the user has to pick it again.
    val needsNewFile: Boolean = false,
    // The first backup right after picking the file failed.
    val failed: Boolean = false
)
