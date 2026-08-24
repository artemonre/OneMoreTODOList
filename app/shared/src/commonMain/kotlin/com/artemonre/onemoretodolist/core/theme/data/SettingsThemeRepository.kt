package com.artemonre.onemoretodolist.core.theme.data

import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.BackgroundOption
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.IconSetOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

private const val KEY_MODE = "theme_mode"
private const val KEY_PALETTE = "theme_palette"
private const val KEY_ICON_SET = "theme_icon_set"
private const val KEY_FONT = "theme_font"
private const val KEY_BACKGROUND = "theme_background"
private const val KEY_ACTION_PLACEMENT = "theme_action_placement"

@OptIn(ExperimentalSettingsApi::class)
class SettingsThemeRepository(
    private val settings: ObservableSettings
) : ThemeRepository {

    override val themeConfig: Flow<ThemeConfig> = combine(
        settings.getStringFlow(KEY_MODE, ThemeMode.System.name),
        settings.getStringFlow(KEY_PALETTE, ColorPaletteOption.Indigo.name),
        settings.getStringFlow(KEY_ICON_SET, IconSetOption.Default.name),
        settings.getStringFlow(KEY_FONT, FontOption.Default.name),
        settings.getStringFlow(KEY_BACKGROUND, BackgroundOption.Solid.name),
        settings.getStringFlow(KEY_ACTION_PLACEMENT, ActionPlacement.End.name)
    ) { values ->
        ThemeConfig(
            mode = values[0].toEnumOrDefault(ThemeMode.System),
            palette = values[1].toEnumOrDefault(ColorPaletteOption.Indigo),
            iconSet = values[2].toEnumOrDefault(IconSetOption.Default),
            font = values[3].toEnumOrDefault(FontOption.Default),
            background = values[4].toEnumOrDefault(BackgroundOption.Solid),
            actionPlacement = values[5].toEnumOrDefault(ActionPlacement.End)
        )
    }

    override suspend fun update(transform: (ThemeConfig) -> ThemeConfig) {
        val next = transform(themeConfig.first())
        settings.putString(KEY_MODE, next.mode.name)
        settings.putString(KEY_PALETTE, next.palette.name)
        settings.putString(KEY_ICON_SET, next.iconSet.name)
        settings.putString(KEY_FONT, next.font.name)
        settings.putString(KEY_BACKGROUND, next.background.name)
        settings.putString(KEY_ACTION_PLACEMENT, next.actionPlacement.name)
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
