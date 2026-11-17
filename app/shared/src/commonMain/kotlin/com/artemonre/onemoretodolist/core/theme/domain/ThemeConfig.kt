package com.artemonre.onemoretodolist.core.theme.domain

data class ThemeConfig(
    val mode: ThemeMode = ThemeMode.System,
    val palette: ColorPaletteOption = ColorPaletteOption.Default,
    // Android 12+ (Material You) only - see isDynamicColorSupported(). Kept independent of
    // [palette] so switching back off dynamic color restores whichever palette was last picked.
    val useDynamicColor: Boolean = false,
    val iconSet: IconSetOption = IconSetOption.Default,
    val font: FontOption = FontOption.Default,
    val background: BackgroundOption = BackgroundOption.Solid,
    val actionPlacement: ActionPlacement = ActionPlacement.End,
    val uiStyle: UiStyleOption = UiStyleOption.Material
)

enum class ThemeMode { System, Light, Dark }

// The visual/component design language, independent of ThemeMode's light/dark color scheme -
// e.g. Material is the flat, standard-elevation look with no customizations;
// Holo is the soft, outlined look with folded card corners and stat tiles
// (designsystem/components/holo). Every style takes its colors from the current palette.
// A stored value that's no longer here (the retired Paper style) falls back to Material when read.
enum class UiStyleOption { Material, Holo }

enum class ColorPaletteOption { Default, Slate, MaterialPaletteGreen, MaterialPaletteYellow }

enum class IconSetOption { Default, Rounded }

enum class FontOption { Default, Serif, Monospace }

enum class BackgroundOption { Solid, Pattern, Image }

enum class ActionPlacement { Start, End }
