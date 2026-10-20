package com.artemonre.onemoretodolist.feature.backup.presentation

import androidx.compose.runtime.State
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import kotlin.time.Instant

// Backup to a hidden app-only folder in the user's own Google Drive. Android-only for now -
// rememberDriveBackup() returns null elsewhere and the Settings section is hidden.
interface DriveBackupController {
    val state: State<DriveBackupState>
    fun backupNow()
    // Downloads the backup; the state's isChoosingRestoreMode then asks Merge/Replace.
    fun restore()
    fun confirmRestore(mode: ImportMode)
    fun cancelRestore()
    fun setAutoBackup(enabled: Boolean)
    fun disconnect()
}

data class DriveBackupState(
    // Access has been granted at some point (and not disconnected since).
    val isConnected: Boolean = false,
    val isBusy: Boolean = false,
    val lastBackupAt: Instant? = null,
    val isAutoBackupEnabled: Boolean = false,
    // The daily backup couldn't get access without the user (e.g. access was revoked from the
    // Google account) - the next manual action asks again.
    val needsReconnect: Boolean = false,
    val isChoosingRestoreMode: Boolean = false,
    val message: DriveBackupMessage? = null
)

sealed interface DriveBackupMessage {
    data object BackedUp : DriveBackupMessage
    data class Restored(val count: Int) : DriveBackupMessage
    data object NoBackupFound : DriveBackupMessage
    // The user declined (or backed out of) Google's consent screen.
    data object AccessDenied : DriveBackupMessage
    // This app build isn't registered with Google for Drive access - a setup problem, not the user's.
    data object NotConfigured : DriveBackupMessage
    data object NoInternet : DriveBackupMessage
    data object Failed : DriveBackupMessage
    data class RestoreFailed(val error: BackupError) : DriveBackupMessage
}
