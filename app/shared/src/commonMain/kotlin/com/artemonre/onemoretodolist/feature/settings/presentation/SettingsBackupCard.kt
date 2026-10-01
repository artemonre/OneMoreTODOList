package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.backup_export
import onemoretodolist.app.shared.generated.resources.backup_export_failed
import onemoretodolist.app.shared.generated.resources.backup_exported
import onemoretodolist.app.shared.generated.resources.backup_file_unreadable
import onemoretodolist.app.shared.generated.resources.backup_import
import onemoretodolist.app.shared.generated.resources.backup_import_invalid
import onemoretodolist.app.shared.generated.resources.backup_import_merge
import onemoretodolist.app.shared.generated.resources.backup_import_mode_message
import onemoretodolist.app.shared.generated.resources.backup_import_mode_title
import onemoretodolist.app.shared.generated.resources.backup_import_newer_format
import onemoretodolist.app.shared.generated.resources.backup_import_nothing_new
import onemoretodolist.app.shared.generated.resources.backup_import_replace
import onemoretodolist.app.shared.generated.resources.backup_import_storage_error
import onemoretodolist.app.shared.generated.resources.backup_imported
import onemoretodolist.app.shared.generated.resources.form_cancel
import onemoretodolist.app.shared.generated.resources.settings_backup
import onemoretodolist.app.shared.generated.resources.settings_backup_description
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// Export/import to a file. fileBackupAvailable is false where the platform has no file dialog
// (see rememberBackupFileExporter) - the buttons are hidden then. extraContent is for
// platform-only backup options that belong in the same card (Google Drive on Android).
@Composable
internal fun SettingsBackupCard(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    fileBackupAvailable: Boolean,
    modifier: Modifier = Modifier,
    extraContent: @Composable () -> Unit = {}
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = stringResource(Res.string.settings_backup),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(Res.string.settings_backup_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (fileBackupAvailable) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onAction(SettingsAction.OnExportClick) },
                        enabled = !state.isBackupBusy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.FileUpload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.backup_export))
                    }
                    OutlinedButton(
                        onClick = { onAction(SettingsAction.OnImportClick) },
                        enabled = !state.isBackupBusy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Filled.FileDownload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(Res.string.backup_import))
                    }
                }
            }
            if (state.isBackupBusy) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
            state.backupMessage?.let { message ->
                Text(
                    text = message.text(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (message.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            extraContent()
        }
    }

    if (state.isChoosingImportMode) {
        ImportModeDialog(
            onModeSelected = { onAction(SettingsAction.OnImportModeSelected(it)) },
            onDismiss = { onAction(SettingsAction.OnImportCancel) }
        )
    }
}

// Shared by file import and Google Drive restore - both ask the same question.
@Composable
internal fun ImportModeDialog(
    onModeSelected: (ImportMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.backup_import_mode_title)) },
        text = { Text(stringResource(Res.string.backup_import_mode_message)) },
        confirmButton = {
            Row {
                TextButton(onClick = { onModeSelected(ImportMode.Replace) }) {
                    Text(stringResource(Res.string.backup_import_replace))
                }
                TextButton(onClick = { onModeSelected(ImportMode.Merge) }) {
                    Text(stringResource(Res.string.backup_import_merge))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.form_cancel))
            }
        }
    )
}

private val BackupMessage.isError: Boolean
    get() = when (this) {
        BackupMessage.Exported, is BackupMessage.Imported -> false
        BackupMessage.ExportFailed, is BackupMessage.ImportFailed, BackupMessage.FileUnreadable -> true
    }

@Composable
private fun BackupMessage.text(): String = when (this) {
    BackupMessage.Exported -> stringResource(Res.string.backup_exported)
    BackupMessage.ExportFailed -> stringResource(Res.string.backup_export_failed)
    is BackupMessage.Imported -> if (count == 0) {
        stringResource(Res.string.backup_import_nothing_new)
    } else {
        pluralStringResource(Res.plurals.backup_imported, count, count)
    }
    is BackupMessage.ImportFailed -> stringResource(error.message())
    BackupMessage.FileUnreadable -> stringResource(Res.string.backup_file_unreadable)
}

private fun BackupError.message() = when (this) {
    BackupError.INVALID_FILE -> Res.string.backup_import_invalid
    BackupError.NEWER_FORMAT -> Res.string.backup_import_newer_format
    BackupError.STORAGE -> Res.string.backup_import_storage_error
}
