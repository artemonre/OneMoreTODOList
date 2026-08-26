package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System
)
