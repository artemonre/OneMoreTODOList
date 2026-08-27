package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val uiStyle: UiStyleOption = UiStyleOption.Material
)
