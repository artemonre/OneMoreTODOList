package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.appVersionName
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.updateFont
import com.artemonre.onemoretodolist.core.theme.domain.updateMode
import com.artemonre.onemoretodolist.core.theme.domain.updatePalette
import com.artemonre.onemoretodolist.core.theme.domain.updateUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.updateUseDynamicColor
import com.artemonre.onemoretodolist.feature.backup.domain.ExportTodos
import com.artemonre.onemoretodolist.feature.backup.domain.ImportMode
import com.artemonre.onemoretodolist.feature.backup.domain.ImportTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.isAppUpdateAvailable
import com.posthog.kmp.PostHog
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L
private const val SETTINGS_SCREEN = "Settings"

private fun captureSettingChanged(section: String, option: String) {
    PostHog.capture(
        event = "setting_changed",
        properties = mapOf("screen" to SETTINGS_SCREEN, "section" to section, "option" to option)
    )
}

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val todoPreferences: TodoPreferences,
    private val exportTodos: ExportTodos,
    private val importTodos: ImportTodos
) : ViewModel() {

    private val appVersion = appVersionName()
    private val updateAvailable = MutableStateFlow(false)
    private val backupUi = MutableStateFlow(BackupUi())

    // The picked file's contents while the Merge/Replace dialog is open - kept out of state, which
    // only needs to know that the dialog is showing.
    private var pendingImportJson: String? = null

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            updateAvailable.value = isAppUpdateAvailable()
        }
    }

    val state = combine(
        themeRepository.themeConfig,
        todoPreferences.archiveCompletedTodos,
        updateAvailable,
        backupUi
    ) { theme, archive, updateAvailable, backup ->
        SettingsState(
            themeMode = theme.mode,
            palette = theme.palette,
            useDynamicColor = theme.useDynamicColor,
            font = theme.font,
            uiStyle = theme.uiStyle,
            archiveCompletedTodos = archive,
            appVersion = appVersion,
            updateAvailable = updateAvailable,
            isChoosingImportMode = backup.isChoosingImportMode,
            isBackupBusy = backup.isBusy,
            backupMessage = backup.message
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), SettingsState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnThemeModeSelected -> {
                captureSettingChanged(section = "Theme", option = action.mode.name)
                viewModelScope.launch {
                    themeRepository.updateMode(action.mode)
                }
            }
            is SettingsAction.OnPaletteSelected -> {
                captureSettingChanged(section = "Palette", option = action.palette.name)
                viewModelScope.launch {
                    themeRepository.updatePalette(action.palette)
                }
            }
            is SettingsAction.OnUseDynamicColorChanged -> {
                captureSettingChanged(
                    section = "Palette",
                    option = if (action.useDynamicColor) "Dynamic Color" else "Palettes"
                )
                viewModelScope.launch {
                    themeRepository.updateUseDynamicColor(action.useDynamicColor)
                }
            }
            is SettingsAction.OnFontSelected -> {
                captureSettingChanged(section = "Font", option = action.font.name)
                viewModelScope.launch {
                    themeRepository.updateFont(action.font)
                }
            }
            is SettingsAction.OnUiStyleSelected -> {
                captureSettingChanged(section = "UI Style", option = action.uiStyle.name)
                viewModelScope.launch {
                    themeRepository.updateUiStyle(action.uiStyle)
                }
            }
            is SettingsAction.OnArchiveCompletedTodosChanged -> {
                captureSettingChanged(
                    section = "Archive completed todos",
                    option = if (action.archive) "On" else "Off"
                )
                viewModelScope.launch {
                    todoPreferences.setArchiveCompletedTodos(action.archive)
                }
            }
            // The actual update flow needs an Activity (Play In-App Updates on Android), which a
            // ViewModel doesn't have - SettingsScreen starts it directly via
            // rememberAppUpdateLauncher() instead of routing it through here.
            is SettingsAction.OnUpdateClick -> Unit
            is SettingsAction.OnExportClick -> export()
            is SettingsAction.OnExportFinished -> backupUi.update {
                it.copy(message = if (action.saved) BackupMessage.Exported else BackupMessage.ExportFailed)
            }
            is SettingsAction.OnImportClick -> viewModelScope.launch { _events.send(SettingsEvent.PickBackupFile) }
            is SettingsAction.OnImportFileRead -> {
                pendingImportJson = action.json
                backupUi.update {
                    if (action.json == null) {
                        it.copy(message = BackupMessage.FileUnreadable)
                    } else {
                        it.copy(isChoosingImportMode = true, message = null)
                    }
                }
            }
            is SettingsAction.OnImportModeSelected -> import(action.mode)
            is SettingsAction.OnImportCancel -> {
                pendingImportJson = null
                backupUi.update { it.copy(isChoosingImportMode = false) }
            }
        }
    }

    private fun export() {
        viewModelScope.launch {
            backupUi.update { it.copy(isBusy = true, message = null) }
            val json = exportTodos()
            backupUi.update { it.copy(isBusy = false) }
            _events.send(SettingsEvent.SaveBackupFile(suggestedName = backupFileName(), json = json))
        }
    }

    private fun import(mode: ImportMode) {
        val json = pendingImportJson ?: return
        pendingImportJson = null
        captureSettingChanged(section = "Import todos", option = mode.name)
        viewModelScope.launch {
            backupUi.update { it.copy(isChoosingImportMode = false, isBusy = true) }
            val message = when (val result = importTodos(json, mode)) {
                is Result.Success -> BackupMessage.Imported(result.data.importedCount)
                is Result.Error -> BackupMessage.ImportFailed(result.error)
            }
            backupUi.update { it.copy(isBusy = false, message = message) }
        }
    }
}

private data class BackupUi(
    val isChoosingImportMode: Boolean = false,
    val isBusy: Boolean = false,
    val message: BackupMessage? = null
)

// onemoretodolist-backup-2026-10-01.json - dated so several exports don't overwrite each other.
private fun backupFileName(): String {
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
    return "onemoretodolist-backup-$today.json"
}
