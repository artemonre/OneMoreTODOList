package com.artemonre.onemoretodolist.core.theme.domain

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.System,
    val palette: ColorPaletteOption = ColorPaletteOption.Indigo,
    val iconSet: IconSetOption = IconSetOption.Default,
    val font: FontOption = FontOption.Default,
    val background: BackgroundOption = BackgroundOption.Solid,
    val actionPlacement: ActionPlacement = ActionPlacement.End
)

enum class ThemeMode { System, Light, Dark }

enum class ColorPaletteOption { Indigo, Slate }

enum class IconSetOption { Default, Rounded }

enum class FontOption { Default, Serif, Monospace }

enum class BackgroundOption { Solid, Pattern, Image }

enum class ActionPlacement { Start, End }
