package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppChipGroup
import com.artemonre.onemoretodolist.core.designsystem.components.AppSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.components.PaletteSwatch
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.isDynamicColorSupported
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import org.koin.compose.viewmodel.koinViewModel

private const val SUPPORT_EMAIL_URI = "mailto:artemonsupport@gmail.com"
private const val PRIVACY_POLICY_URL = "https://artemonre.github.io/OneMoreTODOList/privacy-policy"

private val AVAILABLE_PALETTES = listOf(ColorPaletteOption.Default, ColorPaletteOption.Slate)

// UI-only grouping for the Palette card's tabs - the persisted state is just ThemeConfig's
// useDynamicColor flag; this enum exists only to drive AppSegmentedControl.
private enum class PaletteSourceTab { Palettes, DynamicColor }

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
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
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
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                // Dynamic color only exists on Android 12+ - everywhere else this card looks just
                // like before (a plain "Palette" title, swatches always shown).
                val dynamicColorSupported = isDynamicColorSupported()
                if (dynamicColorSupported) {
                    AppSegmentedControl(
                        options = PaletteSourceTab.entries,
                        selectedOption = if (state.useDynamicColor) {
                            PaletteSourceTab.DynamicColor
                        } else {
                            PaletteSourceTab.Palettes
                        },
                        onOptionSelected = {
                            onAction(SettingsAction.OnUseDynamicColorChanged(it == PaletteSourceTab.DynamicColor))
                        },
                        label = { it.displayName() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "Palette",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!dynamicColorSupported || !state.useDynamicColor) {
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
                } else {
                    Text(
                        text = "Colors are pulled from your wallpaper instead of a fixed palette, " +
                            "and update automatically if you change it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Font",
                    style = MaterialTheme.typography.titleMedium
                )
                AppChipGroup(
                    options = FontOption.entries,
                    selectedOption = state.font,
                    onOptionSelected = { onAction(SettingsAction.OnFontSelected(it)) },
                    label = { it.displayName() },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "UI Style",
                    style = MaterialTheme.typography.titleMedium
                )
                AppChipGroup(
                    // Paper is hidden from the picker for now - dark theme support for it isn't
                    // settled yet. The enum value and its whole implementation stay in place, just
                    // not user-selectable.
                    options = UiStyleOption.entries.filter { it != UiStyleOption.Paper },
                    selectedOption = state.uiStyle,
                    onOptionSelected = { onAction(SettingsAction.OnUiStyleSelected(it)) },
                    label = { it.displayName() },
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Todos",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Archive completed todos",
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.archiveCompletedTodos,
                        onCheckedChange = { onAction(SettingsAction.OnArchiveCompletedTodosChanged(it)) }
                    )
                }
                Text(
                    text = "When off, completing a todo in the app deletes it immediately (Undo is " +
                        "offered via a snackbar). The widget's checkbox always archives instead, to " +
                        "avoid an unrecoverable mis-tap.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column {
                Text(
                    text = "Support",
                    style = MaterialTheme.typography.titleMedium
                )
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(SUPPORT_EMAIL_URI) }
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Email, contentDescription = null)
                    Text("Send an email")
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(PRIVACY_POLICY_URL) }
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.PrivacyTip, contentDescription = null)
                    Text("Privacy policy")
                }
            }
        }
        Text(
            text = "App version: ${state.appVersion}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ThemeMode.displayName(): String = when (this) {
    ThemeMode.System -> "System"
    ThemeMode.Light -> "Light"
    ThemeMode.Dark -> "Dark"
}

private fun PaletteSourceTab.displayName(): String = when (this) {
    PaletteSourceTab.Palettes -> "Palettes"
    PaletteSourceTab.DynamicColor -> "Dynamic color"
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
                palette = ColorPaletteOption.Default,
                font = FontOption.Default,
                uiStyle = UiStyleOption.Material
            ),
            onAction = {}
        )
    }
}
