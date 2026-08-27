package com.artemonre.onemoretodolist.core.theme.domain

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.System,
    val palette: ColorPaletteOption = ColorPaletteOption.Indigo,
    val iconSet: IconSetOption = IconSetOption.Default,
    val font: FontOption = FontOption.Default,
    val background: BackgroundOption = BackgroundOption.Solid,
    val actionPlacement: ActionPlacement = ActionPlacement.End,
    val uiStyle: UiStyleOption = UiStyleOption.Material
)

enum class ThemeMode { System, Light, Dark }

// The visual/component design language, independent of ThemeMode's light/dark color scheme -
// e.g. Material is the flat, standard-elevation look with no customizations; Neumorphic is the
// soft-UI raised/pressed shadow look (designsystem/components/neumorphic); Paper is the
// press-flattening card look the todo list screen currently uses (designsystem/components/paper).
// Not yet wired to swap actual screen components - this is the setting the eventual per-style
// component switch will read.
enum class UiStyleOption { Material, Neumorphic, Paper }

enum class ColorPaletteOption { Indigo, Slate }

enum class IconSetOption { Default, Rounded }

enum class FontOption { Default, Serif, Monospace }

enum class BackgroundOption { Solid, Pattern, Image }

enum class ActionPlacement { Start, End }
