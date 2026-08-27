package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

sealed interface SettingsAction {
    data class OnThemeModeSelected(val mode: ThemeMode) : SettingsAction
    data class OnUiStyleSelected(val uiStyle: UiStyleOption) : SettingsAction
}
