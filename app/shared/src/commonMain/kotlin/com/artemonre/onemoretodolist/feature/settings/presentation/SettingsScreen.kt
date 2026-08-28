package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppChipGroup
import com.artemonre.onemoretodolist.core.designsystem.components.AppSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.components.PaletteSwatch
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import org.koin.compose.viewmodel.koinViewModel

// Only Indigo is offered today - Slate exists in ColorPalettes.kt but isn't ready to present as
// a user-facing choice yet. Add it here once it is.
private val AVAILABLE_PALETTES = listOf(ColorPaletteOption.Indigo)

@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SettingsScreen(state = state, onAction = viewModel::onAction)
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(16.dp)
    ) {
        Text(
            text = "Theme",
            style = MaterialTheme.typography.titleMedium
        )
        AppSegmentedControl(
            options = ThemeMode.entries,
            selectedOption = state.themeMode,
            onOptionSelected = { onAction(SettingsAction.OnThemeModeSelected(it)) },
            label = { it.displayName() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        )
        Text(
            text = "Palette",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(top = 12.dp)
        ) {
            val isDarkTheme = when (state.themeMode) {
                ThemeMode.System -> isSystemInDarkTheme()
                ThemeMode.Light -> false
                ThemeMode.Dark -> true
            }
            AVAILABLE_PALETTES.forEach { option ->
                val palette = option.toColorPalette()
                PaletteSwatch(
                    colorScheme = if (isDarkTheme) palette.dark else palette.light,
                    selected = option == state.palette,
                    onClick = { onAction(SettingsAction.OnPaletteSelected(option)) }
                )
            }
        }
        Text(
            text = "Font",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp)
        )
        AppChipGroup(
            options = FontOption.entries,
            selectedOption = state.font,
            onOptionSelected = { onAction(SettingsAction.OnFontSelected(it)) },
            label = { it.displayName() },
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = "UI Style",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 24.dp)
        )
        AppChipGroup(
            options = UiStyleOption.entries,
            selectedOption = state.uiStyle,
            onOptionSelected = { onAction(SettingsAction.OnUiStyleSelected(it)) },
            label = { it.displayName() },
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

private fun FontOption.displayName(): String = when (this) {
    FontOption.Default -> "Default"
    FontOption.Serif -> "Serif"
    FontOption.Monospace -> "Mono"
}

private fun UiStyleOption.displayName(): String = when (this) {
    UiStyleOption.Material -> "Material"
    UiStyleOption.Paper -> "Paper"
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        SettingsScreen(
            state = SettingsState(
                themeMode = ThemeMode.System,
                palette = ColorPaletteOption.Indigo,
                font = FontOption.Default,
                uiStyle = UiStyleOption.Material
            ),
            onAction = {}
        )
    }
}
