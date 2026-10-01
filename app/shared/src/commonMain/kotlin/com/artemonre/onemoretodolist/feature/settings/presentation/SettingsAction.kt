package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode

sealed interface SettingsAction {
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsAction
    data class OnPaletteSelected(val palette: ColorPaletteOption) : SettingsAction
    data class OnUseDynamicColorChanged(val useDynamicColor: Boolean) : SettingsAction
    data class OnFontSelected(val font: FontOption) : SettingsAction
    data class OnUiStyleSelected(val uiStyle: UiStyleOption) : SettingsAction
    data class OnArchiveCompletedTodosChanged(val archive: Boolean) : SettingsAction
    data object OnUpdateClick : SettingsAction
    data object OnExportClick : SettingsAction
    data class OnExportFinished(val saved: Boolean) : SettingsAction
    data object OnImportClick : SettingsAction
    // json == null: the picked file couldn't be read.
    data class OnImportFileRead(val json: String?) : SettingsAction
    data class OnImportModeSelected(val mode: ImportMode) : SettingsAction
    data object OnImportCancel : SettingsAction
}
