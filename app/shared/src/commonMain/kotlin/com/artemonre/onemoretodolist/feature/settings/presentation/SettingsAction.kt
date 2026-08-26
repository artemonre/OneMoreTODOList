package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode

sealed interface SettingsAction {
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsAction
}
