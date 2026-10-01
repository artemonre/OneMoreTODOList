package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupMessage
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.drive_access_denied
import onemoretodolist.app.shared.generated.resources.drive_backed_up
import onemoretodolist.app.shared.generated.resources.drive_backup_failed
import onemoretodolist.app.shared.generated.resources.drive_description
import onemoretodolist.app.shared.generated.resources.drive_disconnect
import onemoretodolist.app.shared.generated.resources.drive_needs_reconnect
import onemoretodolist.app.shared.generated.resources.drive_no_backup_found
import onemoretodolist.app.shared.generated.resources.drive_no_internet
import onemoretodolist.app.shared.generated.resources.drive_not_configured
import org.jetbrains.compose.resources.stringResource

// The Google Drive tab of SettingsBackupCard (Android only).
@Composable
internal fun DriveBackupTab(controller: DriveBackupController) {
    val state = controller.state.value
    BackupTabDescription(Res.string.drive_description)
    BackupButtons(
        backupIcon = Icons.Filled.CloudUpload,
        restoreIcon = Icons.Filled.CloudDownload,
        onBackup = controller::backupNow,
        onRestore = controller::restore,
        enabled = !state.isBusy
    )
    AutoBackupSwitch(
        checked = state.isAutoBackupEnabled,
        onCheckedChange = controller::setAutoBackup,
        enabled = !state.isBusy,
        lastBackupAt = state.lastBackupAt
    )
    if (state.needsReconnect) BackupStatusText(stringResource(Res.string.drive_needs_reconnect), isError = true)
    BackupBusyIndicator(state.isBusy)
    state.message?.let { message -> BackupStatusText(message.text(), message.isError) }
    if (state.isConnected) {
        TextButton(
            onClick = controller::disconnect,
            enabled = !state.isBusy,
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(stringResource(Res.string.drive_disconnect))
        }
    }

    if (state.isChoosingRestoreMode) {
        ImportModeDialog(
            onModeSelected = controller::confirmRestore,
            onDismiss = controller::cancelRestore
        )
    }
}

private val DriveBackupMessage.isError: Boolean
    get() = when (this) {
        DriveBackupMessage.BackedUp, is DriveBackupMessage.Restored, DriveBackupMessage.NoBackupFound -> false
        DriveBackupMessage.AccessDenied, DriveBackupMessage.NotConfigured, DriveBackupMessage.NoInternet,
        DriveBackupMessage.Failed, is DriveBackupMessage.RestoreFailed -> true
    }

@Composable
private fun DriveBackupMessage.text(): String = when (this) {
    DriveBackupMessage.BackedUp -> stringResource(Res.string.drive_backed_up)
    is DriveBackupMessage.Restored -> importedText(count)
    DriveBackupMessage.NoBackupFound -> stringResource(Res.string.drive_no_backup_found)
    DriveBackupMessage.AccessDenied -> stringResource(Res.string.drive_access_denied)
    DriveBackupMessage.NotConfigured -> stringResource(Res.string.drive_not_configured)
    DriveBackupMessage.NoInternet -> stringResource(Res.string.drive_no_internet)
    DriveBackupMessage.Failed -> stringResource(Res.string.drive_backup_failed)
    is DriveBackupMessage.RestoreFailed -> stringResource(error.message())
}
