package com.artemonre.onemoretodolist.core.theme.data

import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.BackgroundOption
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.IconSetOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

private const val KEY_MODE = "theme_mode"
private const val KEY_PALETTE = "theme_palette"
private const val KEY_USE_DYNAMIC_COLOR = "theme_use_dynamic_color"
private const val KEY_ICON_SET = "theme_icon_set"
private const val KEY_FONT = "theme_font"
private const val KEY_BACKGROUND = "theme_background"
private const val KEY_ACTION_PLACEMENT = "theme_action_placement"
private const val KEY_UI_STYLE = "theme_ui_style"

@OptIn(ExperimentalSettingsApi::class)
class SettingsThemeRepository(
    private val settings: ObservableSettings
) : ThemeRepository {

    override val themeConfig: Flow<ThemeConfig> = combine(
        settings.getStringFlow(KEY_MODE, ThemeMode.System.name),
        settings.getStringFlow(KEY_PALETTE, ColorPaletteOption.Default.name),
        // A string, not settings.getBooleanFlow, so this stays in the same combine() overload as
        // every other field here - combine()'s vararg overload requires one Flow<T> for all of
        // them, and mixing in a single Flow<Boolean> makes T's inference degenerate.
        settings.getStringFlow(KEY_USE_DYNAMIC_COLOR, false.toString()),
        settings.getStringFlow(KEY_ICON_SET, IconSetOption.Default.name),
        settings.getStringFlow(KEY_FONT, FontOption.Default.name),
        settings.getStringFlow(KEY_BACKGROUND, BackgroundOption.Solid.name),
        settings.getStringFlow(KEY_ACTION_PLACEMENT, ActionPlacement.End.name),
        settings.getStringFlow(KEY_UI_STYLE, UiStyleOption.Material.name)
    ) { values ->
        ThemeConfig(
            mode = values[0].toEnumOrDefault(ThemeMode.System),
            palette = values[1].toEnumOrDefault(ColorPaletteOption.Default),
            useDynamicColor = values[2].toBoolean(),
            iconSet = values[3].toEnumOrDefault(IconSetOption.Default),
            font = values[4].toEnumOrDefault(FontOption.Default),
            background = values[5].toEnumOrDefault(BackgroundOption.Solid),
            actionPlacement = values[6].toEnumOrDefault(ActionPlacement.End),
            uiStyle = values[7].toEnumOrDefault(UiStyleOption.Material)
        )
    }

    override suspend fun update(transform: (ThemeConfig) -> ThemeConfig) {
        val next = transform(themeConfig.first())
        settings.putString(KEY_MODE, next.mode.name)
        settings.putString(KEY_PALETTE, next.palette.name)
        settings.putString(KEY_USE_DYNAMIC_COLOR, next.useDynamicColor.toString())
        settings.putString(KEY_ICON_SET, next.iconSet.name)
        settings.putString(KEY_FONT, next.font.name)
        settings.putString(KEY_BACKGROUND, next.background.name)
        settings.putString(KEY_ACTION_PLACEMENT, next.actionPlacement.name)
        settings.putString(KEY_UI_STYLE, next.uiStyle.name)
    }
}

private inline fun <reified T : Enum<T>> String.toEnumOrDefault(default: T): T =
    enumValues<T>().firstOrNull { it.name == this } ?: default
