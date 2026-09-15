package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.appVersionName
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.updateFont
import com.artemonre.onemoretodolist.core.theme.domain.updateMode
import com.artemonre.onemoretodolist.core.theme.domain.updatePalette
import com.artemonre.onemoretodolist.core.theme.domain.updateUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.updateUseDynamicColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.posthog.kmp.PostHog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val todoPreferences: TodoPreferences
) : ViewModel() {

    private val appVersion = appVersionName()

    val state = combine(themeRepository.themeConfig, todoPreferences.archiveCompletedTodos) { theme, archive ->
        SettingsState(
            themeMode = theme.mode,
            palette = theme.palette,
            useDynamicColor = theme.useDynamicColor,
            font = theme.font,
            uiStyle = theme.uiStyle,
            archiveCompletedTodos = archive,
            appVersion = appVersion
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
        }
    }
}
