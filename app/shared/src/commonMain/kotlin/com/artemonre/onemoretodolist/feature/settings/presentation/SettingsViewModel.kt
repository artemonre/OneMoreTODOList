package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.updateFont
import com.artemonre.onemoretodolist.core.theme.domain.updateMode
import com.artemonre.onemoretodolist.core.theme.domain.updatePalette
import com.artemonre.onemoretodolist.core.theme.domain.updateUiStyle
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

class SettingsViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val state = themeRepository.themeConfig
        .map { SettingsState(themeMode = it.mode, palette = it.palette, font = it.font, uiStyle = it.uiStyle) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), SettingsState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnThemeModeSelected -> {
                viewModelScope.launch {
                    themeRepository.updateMode(action.mode)
                }
            }
            is SettingsAction.OnPaletteSelected -> {
                viewModelScope.launch {
                    themeRepository.updatePalette(action.palette)
                }
            }
            is SettingsAction.OnFontSelected -> {
                viewModelScope.launch {
                    themeRepository.updateFont(action.font)
                }
            }
            is SettingsAction.OnUiStyleSelected -> {
                viewModelScope.launch {
                    themeRepository.updateUiStyle(action.uiStyle)
                }
            }
        }
    }
}
