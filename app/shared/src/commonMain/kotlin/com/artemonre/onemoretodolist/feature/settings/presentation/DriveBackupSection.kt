package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupMessage
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.backup_import_invalid
import onemoretodolist.app.shared.generated.resources.backup_import_newer_format
import onemoretodolist.app.shared.generated.resources.backup_import_nothing_new
import onemoretodolist.app.shared.generated.resources.backup_import_storage_error
import onemoretodolist.app.shared.generated.resources.backup_imported
import onemoretodolist.app.shared.generated.resources.drive_access_denied
import onemoretodolist.app.shared.generated.resources.drive_auto_backup
import onemoretodolist.app.shared.generated.resources.drive_backed_up
import onemoretodolist.app.shared.generated.resources.drive_backup_failed
import onemoretodolist.app.shared.generated.resources.drive_backup_now
import onemoretodolist.app.shared.generated.resources.drive_description
import onemoretodolist.app.shared.generated.resources.drive_disconnect
import onemoretodolist.app.shared.generated.resources.drive_last_backup
import onemoretodolist.app.shared.generated.resources.drive_needs_reconnect
import onemoretodolist.app.shared.generated.resources.drive_no_backup_found
import onemoretodolist.app.shared.generated.resources.drive_no_internet
import onemoretodolist.app.shared.generated.resources.drive_not_configured
import onemoretodolist.app.shared.generated.resources.drive_restore
import onemoretodolist.app.shared.generated.resources.drive_title
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

private val lastBackupFormat = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    hour()
    char(':')
    minute()
}

// Google Drive backup, shown inside SettingsBackupCard where the platform has it (Android).
@Composable
internal fun DriveBackupSection(controller: DriveBackupController) {
    val state = controller.state.value
    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
        Text(
            text = stringResource(Res.string.drive_title),
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = stringResource(Res.string.drive_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = controller::backupNow,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.drive_backup_now))
            }
            OutlinedButton(
                onClick = controller::restore,
                enabled = !state.isBusy,
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Filled.CloudDownload, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(Res.string.drive_restore))
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.drive_auto_backup),
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = state.isAutoBackupEnabled,
                onCheckedChange = controller::setAutoBackup,
                enabled = !state.isBusy
            )
        }
        state.lastBackupAt?.let { lastBackupAt ->
            Text(
                text = stringResource(Res.string.drive_last_backup, lastBackupAt.formatLocal()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (state.needsReconnect) {
            Text(
                text = stringResource(Res.string.drive_needs_reconnect),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        if (state.isBusy) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            )
        }
        state.message?.let { message ->
            Text(
                text = message.text(),
                style = MaterialTheme.typography.bodySmall,
                color = if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        if (state.isConnected) {
            TextButton(
                onClick = controller::disconnect,
                enabled = !state.isBusy,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(stringResource(Res.string.drive_disconnect))
            }
        }
    }

    if (state.isChoosingRestoreMode) {
        ImportModeDialog(
            onModeSelected = controller::confirmRestore,
            onDismiss = controller::cancelRestore
        )
    }
}

private fun Instant.formatLocal(): String =
    lastBackupFormat.format(toLocalDateTime(TimeZone.currentSystemDefault()))

private val DriveBackupMessage.isError: Boolean
    get() = when (this) {
        DriveBackupMessage.BackedUp, is DriveBackupMessage.Restored, DriveBackupMessage.NoBackupFound -> false
        DriveBackupMessage.AccessDenied, DriveBackupMessage.NotConfigured, DriveBackupMessage.NoInternet, DriveBackupMessage.Failed,
        is DriveBackupMessage.RestoreFailed -> true
    }

@Composable
private fun DriveBackupMessage.text(): String = when (this) {
    DriveBackupMessage.BackedUp -> stringResource(Res.string.drive_backed_up)
    is DriveBackupMessage.Restored -> if (count == 0) {
        stringResource(Res.string.backup_import_nothing_new)
    } else {
        pluralStringResource(Res.plurals.backup_imported, count, count)
    }
    DriveBackupMessage.NoBackupFound -> stringResource(Res.string.drive_no_backup_found)
    DriveBackupMessage.AccessDenied -> stringResource(Res.string.drive_access_denied)
    DriveBackupMessage.NotConfigured -> stringResource(Res.string.drive_not_configured)
    DriveBackupMessage.NoInternet -> stringResource(Res.string.drive_no_internet)
    DriveBackupMessage.Failed -> stringResource(Res.string.drive_backup_failed)
    is DriveBackupMessage.RestoreFailed -> stringResource(
        when (error) {
            BackupError.INVALID_FILE -> Res.string.backup_import_invalid
            BackupError.NEWER_FORMAT -> Res.string.backup_import_newer_format
            BackupError.STORAGE -> Res.string.backup_import_storage_error
        }
    )
}
