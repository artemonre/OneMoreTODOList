package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import com.artemonre.onemoretodolist.feature.backup.domain.BackupError

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val palette: ColorPaletteOption = ColorPaletteOption.Default,
    val useDynamicColor: Boolean = false,
    val font: FontOption = FontOption.Default,
    val uiStyle: UiStyleOption = UiStyleOption.Material,
    val archiveCompletedTodos: Boolean = true,
    val appVersion: String = "",
    val updateAvailable: Boolean = false,
    // A backup file has been read and is waiting for the user to pick Merge or Replace.
    val isChoosingImportMode: Boolean = false,
    val isBackupBusy: Boolean = false,
    // The outcome of the last export/import, shown in the Backup card until the next one.
    val backupMessage: BackupMessage? = null
)

sealed interface BackupMessage {
    data object Exported : BackupMessage
    data object ExportFailed : BackupMessage
    data class Imported(val count: Int) : BackupMessage
    data class ImportFailed(val error: BackupError) : BackupMessage
    data object FileUnreadable : BackupMessage
}
