package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.presentation.resourceLabels
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupController
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.backup_auto_daily
import onemoretodolist.app.shared.generated.resources.backup_export_failed
import onemoretodolist.app.shared.generated.resources.backup_exported
import onemoretodolist.app.shared.generated.resources.backup_file_auto_needs_new_file
import onemoretodolist.app.shared.generated.resources.backup_file_unreadable
import onemoretodolist.app.shared.generated.resources.backup_import_invalid
import onemoretodolist.app.shared.generated.resources.backup_import_merge
import onemoretodolist.app.shared.generated.resources.backup_import_mode_message
import onemoretodolist.app.shared.generated.resources.backup_import_mode_title
import onemoretodolist.app.shared.generated.resources.backup_import_newer_format
import onemoretodolist.app.shared.generated.resources.backup_import_nothing_new
import onemoretodolist.app.shared.generated.resources.backup_import_replace
import onemoretodolist.app.shared.generated.resources.backup_import_storage_error
import onemoretodolist.app.shared.generated.resources.backup_imported
import onemoretodolist.app.shared.generated.resources.backup_last_backup
import onemoretodolist.app.shared.generated.resources.backup_now
import onemoretodolist.app.shared.generated.resources.backup_restore
import onemoretodolist.app.shared.generated.resources.backup_tab_file
import onemoretodolist.app.shared.generated.resources.drive_title
import onemoretodolist.app.shared.generated.resources.form_cancel
import onemoretodolist.app.shared.generated.resources.settings_backup
import onemoretodolist.app.shared.generated.resources.settings_backup_description
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource

// Two buttons share one row, so the stock 24dp side padding leaves little room for the label.
private val BACKUP_BUTTON_CONTENT_PADDING = PaddingValues(AppSpacing.s)

// UI-only: which backup option the card shows, in tab order. Not persisted - the card always opens
// on Google Drive where it's available, File everywhere else.
private enum class BackupTab { GoogleDrive, File }

private val lastBackupFormat = LocalDateTime.Format {
    date(LocalDate.Formats.ISO)
    char(' ')
    hour()
    char(':')
    minute()
}

// The two backup options as tabs: Google Drive (Android only - the tabs disappear where driveBackup
// is null) and a file (every platform). fileBackupAvailable is false where the platform has
// no file dialog (see rememberBackupFileExporter); fileAutoBackup is null where daily automatic
// file backup isn't possible (see rememberFileAutoBackup).
@Composable
internal fun SettingsBackupCard(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    fileBackupAvailable: Boolean,
    driveBackup: DriveBackupController?,
    fileAutoBackup: FileAutoBackupController?,
    modifier: Modifier = Modifier
) {
    var selectedTab by rememberSaveable {
        mutableStateOf(if (driveBackup != null) BackupTab.GoogleDrive else BackupTab.File)
    }
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
            Text(
                text = stringResource(Res.string.settings_backup),
                style = MaterialTheme.typography.titleMedium
            )
            if (driveBackup != null) {
                AppSegmentedControl(
                    options = BackupTab.entries,
                    selectedOption = selectedTab,
                    onOptionSelected = { selectedTab = it },
                    label = resourceLabels(BackupTab.entries) { it.displayName() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.m)
                )
            }
            if (driveBackup != null && selectedTab == BackupTab.GoogleDrive) {
                DriveBackupTab(controller = driveBackup)
            } else {
                FileBackupTab(
                    state = state,
                    onAction = onAction,
                    fileBackupAvailable = fileBackupAvailable,
                    fileAutoBackup = fileAutoBackup
                )
            }
        }
    }

    if (state.isChoosingImportMode) {
        ImportModeDialog(
            onModeSelected = { onAction(SettingsAction.OnImportModeSelected(it)) },
            onDismiss = { onAction(SettingsAction.OnImportCancel) }
        )
    }
}

@Composable
private fun FileBackupTab(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    fileBackupAvailable: Boolean,
    fileAutoBackup: FileAutoBackupController?
) {
    val autoState = fileAutoBackup?.state?.value
    val isBusy = state.isBackupBusy || autoState?.isBusy == true
    BackupTabDescription(Res.string.settings_backup_description)
    if (fileBackupAvailable) {
        BackupButtons(
            backupIcon = Icons.Filled.FileUpload,
            restoreIcon = Icons.Filled.FileDownload,
            onBackup = { onAction(SettingsAction.OnExportClick) },
            onRestore = { onAction(SettingsAction.OnImportClick) },
            enabled = !isBusy
        )
    }
    if (fileAutoBackup != null && autoState != null) {
        AutoBackupSwitch(
            checked = autoState.isEnabled,
            onCheckedChange = fileAutoBackup::setEnabled,
            enabled = !isBusy,
            lastBackupAt = autoState.lastBackupAt
        )
        if (autoState.needsNewFile) BackupStatusText(stringResource(Res.string.backup_file_auto_needs_new_file), isError = true)
        if (autoState.failed) BackupStatusText(stringResource(Res.string.backup_export_failed), isError = true)
    }
    BackupBusyIndicator(isBusy)
    state.backupMessage?.let { message -> BackupStatusText(message.text(), message.isError) }
}

// --- Pieces shared by both tabs ---

@Composable
internal fun BackupTabDescription(text: StringResource) {
    Text(
        text = stringResource(text),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppSpacing.m)
    )
}

@Composable
internal fun BackupButtons(
    backupIcon: ImageVector,
    restoreIcon: ImageVector,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.m),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.m)
    ) {
        OutlinedButton(
            onClick = onBackup,
            enabled = enabled,
            contentPadding = BACKUP_BUTTON_CONTENT_PADDING,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = backupIcon, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.backup_now))
        }
        OutlinedButton(
            onClick = onRestore,
            enabled = enabled,
            contentPadding = BACKUP_BUTTON_CONTENT_PADDING,
            modifier = Modifier.weight(1f)
        ) {
            Icon(imageVector = restoreIcon, contentDescription = null)
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(stringResource(Res.string.backup_restore))
        }
    }
}

@Composable
internal fun AutoBackupSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean,
    lastBackupAt: Instant?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.m),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.backup_auto_daily),
            modifier = Modifier.weight(1f)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
    lastBackupAt?.let {
        Text(
            text = stringResource(Res.string.backup_last_backup, lastBackupFormat.format(it.toLocalDateTime(TimeZone.currentSystemDefault()))),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = AppSpacing.xs)
        )
    }
}

@Composable
internal fun BackupBusyIndicator(isBusy: Boolean) {
    if (isBusy) {
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = AppSpacing.m)
        )
    }
}

@Composable
internal fun BackupStatusText(text: String, isError: Boolean) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = AppSpacing.s)
    )
}

// Shared by file import and Google Drive restore - both ask the same question.
@Composable
internal fun ImportModeDialog(
    onModeSelected: (ImportMode) -> Unit,
    onDismiss: () -> Unit
) {
    MaterialAlertDialog(
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

// "Imported N todos" - shared by file import and Drive restore.
@Composable
internal fun importedText(count: Int): String = if (count == 0) {
    stringResource(Res.string.backup_import_nothing_new)
} else {
    pluralStringResource(Res.plurals.backup_imported, count, count)
}

internal fun BackupError.message(): StringResource = when (this) {
    BackupError.INVALID_FILE -> Res.string.backup_import_invalid
    BackupError.NEWER_FORMAT -> Res.string.backup_import_newer_format
    BackupError.STORAGE -> Res.string.backup_import_storage_error
}

private fun BackupTab.displayName(): StringResource = when (this) {
    BackupTab.File -> Res.string.backup_tab_file
    BackupTab.GoogleDrive -> Res.string.drive_title
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
    is BackupMessage.Imported -> importedText(count)
    is BackupMessage.ImportFailed -> stringResource(error.message())
    BackupMessage.FileUnreadable -> stringResource(Res.string.backup_file_unreadable)
}
