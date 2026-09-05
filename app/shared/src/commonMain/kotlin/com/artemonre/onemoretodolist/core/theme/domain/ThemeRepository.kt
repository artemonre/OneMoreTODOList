package com.artemonre.onemoretodolist.core.theme.domain

import kotlinx.coroutines.flow.Flow

interface ThemeRepository {
    val themeConfig: Flow<ThemeConfig>
    suspend fun update(transform: (ThemeConfig) -> ThemeConfig)
}

suspend fun ThemeRepository.updateMode(mode: ThemeMode) = update { it.copy(mode = mode) }
suspend fun ThemeRepository.updatePalette(palette: ColorPaletteOption) = update { it.copy(palette = palette) }
suspend fun ThemeRepository.updateIconSet(iconSet: IconSetOption) = update { it.copy(iconSet = iconSet) }
suspend fun ThemeRepository.updateFont(font: FontOption) = update { it.copy(font = font) }
suspend fun ThemeRepository.updateBackground(background: BackgroundOption) = update { it.copy(background = background) }
suspend fun ThemeRepository.updateActionPlacement(placement: ActionPlacement) = update { it.copy(actionPlacement = placement) }
suspend fun ThemeRepository.updateUiStyle(uiStyle: UiStyleOption) = update { it.copy(uiStyle = uiStyle) }
