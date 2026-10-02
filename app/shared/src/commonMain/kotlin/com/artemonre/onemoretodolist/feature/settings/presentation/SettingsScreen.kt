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
import androidx.compose.material3.Button
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.designsystem.components.AppCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppChipGroup
import com.artemonre.onemoretodolist.core.designsystem.components.AppSegmentedControl
import com.artemonre.onemoretodolist.core.designsystem.components.PaletteSwatch
import com.artemonre.onemoretodolist.core.designsystem.theme.AppSpacing
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.designsystem.theme.isDynamicColorSupported
import com.artemonre.onemoretodolist.core.designsystem.theme.toColorPalette
import com.artemonre.onemoretodolist.core.presentation.ObserveAsEvents
import com.artemonre.onemoretodolist.core.presentation.resourceLabels
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import com.artemonre.onemoretodolist.feature.backup.presentation.DriveBackupController
import com.artemonre.onemoretodolist.feature.backup.presentation.FileAutoBackupController
import com.artemonre.onemoretodolist.rememberAppUpdateLauncher
import com.artemonre.onemoretodolist.rememberBackupFileExporter
import com.artemonre.onemoretodolist.rememberBackupFileImporter
import com.artemonre.onemoretodolist.rememberDriveBackup
import com.artemonre.onemoretodolist.rememberFileAutoBackup
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.settings_app_version
import onemoretodolist.app.shared.generated.resources.settings_archive_completed
import onemoretodolist.app.shared.generated.resources.settings_archive_completed_description
import onemoretodolist.app.shared.generated.resources.settings_dynamic_color
import onemoretodolist.app.shared.generated.resources.settings_dynamic_color_description
import onemoretodolist.app.shared.generated.resources.settings_font
import onemoretodolist.app.shared.generated.resources.settings_font_default
import onemoretodolist.app.shared.generated.resources.settings_font_mono
import onemoretodolist.app.shared.generated.resources.settings_font_serif
import onemoretodolist.app.shared.generated.resources.settings_palette
import onemoretodolist.app.shared.generated.resources.settings_palettes
import onemoretodolist.app.shared.generated.resources.settings_privacy_policy
import onemoretodolist.app.shared.generated.resources.settings_send_email
import onemoretodolist.app.shared.generated.resources.settings_support
import onemoretodolist.app.shared.generated.resources.settings_theme
import onemoretodolist.app.shared.generated.resources.settings_theme_dark
import onemoretodolist.app.shared.generated.resources.settings_theme_light
import onemoretodolist.app.shared.generated.resources.settings_theme_system
import onemoretodolist.app.shared.generated.resources.settings_todos
import onemoretodolist.app.shared.generated.resources.settings_ui_style
import onemoretodolist.app.shared.generated.resources.settings_ui_style_material
import onemoretodolist.app.shared.generated.resources.settings_ui_style_paper
import onemoretodolist.app.shared.generated.resources.settings_update
import onemoretodolist.app.shared.generated.resources.settings_update_available
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val SUPPORT_EMAIL_URI = "mailto:artemonsupport@gmail.com"
private const val PRIVACY_POLICY_URL = "https://artemonre.github.io/OneMoreTODOList/privacy-policy"

private val AVAILABLE_PALETTES = listOf(ColorPaletteOption.Default, ColorPaletteOption.Slate)

// Extra inner padding for settings sections on top of the card's own 8dp - together one spacing
// step up (12dp), since these hold several rows of controls. Shared with SettingsBackupCard.
internal val SETTINGS_CARD_CONTENT_PADDING = AppSpacing.xs

// UI-only grouping for the Palette card's tabs - the persisted state is just ThemeConfig's
// useDynamicColor flag; this enum exists only to drive AppSegmentedControl.
private enum class PaletteSourceTab { Palettes, DynamicColor }

@Composable
fun SettingsRoot(
    viewModel: SettingsViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val saveBackupFile = rememberBackupFileExporter(
        onFinished = { saved -> viewModel.onAction(SettingsAction.OnExportFinished(saved)) }
    )
    val pickBackupFile = rememberBackupFileImporter(
        onJsonRead = { json -> viewModel.onAction(SettingsAction.OnImportFileRead(json)) }
    )
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.SaveBackupFile -> saveBackupFile?.invoke(event.suggestedName, event.json)
            SettingsEvent.PickBackupFile -> pickBackupFile?.invoke()
        }
    }
    SettingsScreen(
        state = state,
        onAction = viewModel::onAction,
        fileBackupAvailable = saveBackupFile != null && pickBackupFile != null,
        driveBackup = rememberDriveBackup(),
        fileAutoBackup = rememberFileAutoBackup()
    )
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    fileBackupAvailable: Boolean = true,
    // Null where Google Drive backup isn't available (anywhere but Android) - and in previews.
    driveBackup: DriveBackupController? = null,
    // Null where daily automatic file backup isn't available (anywhere but Android) - and in previews.
    fileAutoBackup: FileAutoBackupController? = null
) {
    val startAppUpdate = rememberAppUpdateLauncher()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.l),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.l)
    ) {
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
                Text(
                    text = stringResource(Res.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium
                )
                AppSegmentedControl(
                    options = ThemeMode.entries,
                    selectedOption = state.themeMode,
                    onOptionSelected = { onAction(SettingsAction.OnThemeModeSelected(it)) },
                    label = resourceLabels(ThemeMode.entries) { it.displayName() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.m)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
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
                        label = resourceLabels(PaletteSourceTab.entries) { it.displayName() },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = stringResource(Res.string.settings_palette),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!dynamicColorSupported || !state.useDynamicColor) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
                        modifier = Modifier.padding(top = AppSpacing.m)
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
                        text = stringResource(Res.string.settings_dynamic_color_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = AppSpacing.m)
                    )
                }
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
                Text(
                    text = stringResource(Res.string.settings_font),
                    style = MaterialTheme.typography.titleMedium
                )
                AppChipGroup(
                    options = FontOption.entries,
                    selectedOption = state.font,
                    onOptionSelected = { onAction(SettingsAction.OnFontSelected(it)) },
                    label = resourceLabels(FontOption.entries) { it.displayName() },
                    modifier = Modifier.padding(top = AppSpacing.m)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
                Text(
                    text = stringResource(Res.string.settings_ui_style),
                    style = MaterialTheme.typography.titleMedium
                )
                AppChipGroup(
                    // Paper is hidden from the picker for now - dark theme support for it isn't
                    // settled yet. The enum value and its whole implementation stay in place, just
                    // not user-selectable.
                    options = UiStyleOption.entries.filter { it != UiStyleOption.Paper },
                    selectedOption = state.uiStyle,
                    onOptionSelected = { onAction(SettingsAction.OnUiStyleSelected(it)) },
                    label = resourceLabels(UiStyleOption.entries) { it.displayName() },
                    modifier = Modifier.padding(top = AppSpacing.m)
                )
            }
        }
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
                Text(
                    text = stringResource(Res.string.settings_todos),
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppSpacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.settings_archive_completed),
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.archiveCompletedTodos,
                        onCheckedChange = { onAction(SettingsAction.OnArchiveCompletedTodosChanged(it)) }
                    )
                }
                Text(
                    text = stringResource(Res.string.settings_archive_completed_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = AppSpacing.xs)
                )
            }
        }
        SettingsBackupCard(
            state = state,
            onAction = onAction,
            fileBackupAvailable = fileBackupAvailable,
            driveBackup = driveBackup,
            fileAutoBackup = fileAutoBackup
        )
        AppCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SETTINGS_CARD_CONTENT_PADDING)) {
                Text(
                    text = stringResource(Res.string.settings_support),
                    style = MaterialTheme.typography.titleMedium
                )
                val uriHandler = LocalUriHandler.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(SUPPORT_EMAIL_URI) }
                        .padding(top = AppSpacing.m),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Email, contentDescription = null)
                    Text(stringResource(Res.string.settings_send_email))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { uriHandler.openUri(PRIVACY_POLICY_URL) }
                        .padding(top = AppSpacing.m),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.m),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.PrivacyTip, contentDescription = null)
                    Text(stringResource(Res.string.settings_privacy_policy))
                }
            }
        }
        if (state.updateAvailable) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SETTINGS_CARD_CONTENT_PADDING),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(Res.string.settings_update_available),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = {
                            onAction(SettingsAction.OnUpdateClick)
                            startAppUpdate?.invoke()
                        }
                    ) {
                        Text(stringResource(Res.string.settings_update))
                    }
                }
            }
        }
        Text(
            text = stringResource(Res.string.settings_app_version, state.appVersion),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun ThemeMode.displayName(): StringResource = when (this) {
    ThemeMode.System -> Res.string.settings_theme_system
    ThemeMode.Light -> Res.string.settings_theme_light
    ThemeMode.Dark -> Res.string.settings_theme_dark
}

private fun PaletteSourceTab.displayName(): StringResource = when (this) {
    PaletteSourceTab.Palettes -> Res.string.settings_palettes
    PaletteSourceTab.DynamicColor -> Res.string.settings_dynamic_color
}

private fun FontOption.displayName(): StringResource = when (this) {
    FontOption.Default -> Res.string.settings_font_default
    FontOption.Serif -> Res.string.settings_font_serif
    FontOption.Monospace -> Res.string.settings_font_mono
}

private fun UiStyleOption.displayName(): StringResource = when (this) {
    UiStyleOption.Material -> Res.string.settings_ui_style_material
    UiStyleOption.Paper -> Res.string.settings_ui_style_paper
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

@Preview
@Composable
private fun SettingsScreenUpdateAvailablePreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        SettingsScreen(
            state = SettingsState(
                themeMode = ThemeMode.System,
                palette = ColorPaletteOption.Default,
                font = FontOption.Default,
                uiStyle = UiStyleOption.Material,
                updateAvailable = true
            ),
            onAction = {}
        )
    }
}
