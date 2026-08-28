package com.artemonre.onemoretodolist.feature.settings.presentation

import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

data class SettingsState(
    val themeMode: ThemeMode = ThemeMode.System,
    val palette: ColorPaletteOption = ColorPaletteOption.Default,
    val font: FontOption = FontOption.Default,
    val uiStyle: UiStyleOption = UiStyleOption.Material
)
