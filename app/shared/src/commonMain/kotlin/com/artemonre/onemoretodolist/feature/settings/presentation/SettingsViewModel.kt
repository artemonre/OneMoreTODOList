package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.updateMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val STATE_STOP_TIMEOUT_MILLIS = 5_000L

class SettingsViewModel(
    private val themeRepository: ThemeRepository
) : ViewModel() {

    val state = themeRepository.themeConfig
        .map { SettingsState(themeMode = it.mode) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STATE_STOP_TIMEOUT_MILLIS), SettingsState())

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnThemeModeSelected -> {
                viewModelScope.launch {
                    themeRepository.updateMode(action.mode)
                }
            }
        }
    }
}
