package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.runComposeUiTest
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ColorPaletteOption
import com.artemonre.onemoretodolist.core.theme.domain.FontOption
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import kotlin.test.Test
import kotlinx.coroutines.runBlocking
import onemoretodolist.app.shared.generated.resources.Res
import onemoretodolist.app.shared.generated.resources.settings_backup
import onemoretodolist.app.shared.generated.resources.settings_font
import onemoretodolist.app.shared.generated.resources.settings_colors
import onemoretodolist.app.shared.generated.resources.settings_support
import onemoretodolist.app.shared.generated.resources.settings_theme
import onemoretodolist.app.shared.generated.resources.settings_todos
import onemoretodolist.app.shared.generated.resources.settings_ui_style
import org.jetbrains.compose.resources.getString

// TalkBack's "Headings" reading control jumps between these section titles.
@OptIn(ExperimentalTestApi::class)
class SettingsAccessibilityTest {

    @Test
    fun `every section title is a heading`() = runComposeUiTest {
        showSettings(UiStyleOption.Material)
        assertEveryTitleIsAHeading()
    }

    // Holo numbers its sections and shows them in capitals ("01 // THEME") - both are decoration,
    // TalkBack reads the title as written.
    @Test
    fun `Holo section titles are headings without their numbers or capitals`() = runComposeUiTest {
        showSettings(UiStyleOption.Holo)
        assertEveryTitleIsAHeading()
        onAllNodes(hasText("//", substring = true)).assertCountEquals(0)
        onAllNodes(hasText(runBlocking { getString(Res.string.settings_theme) }.uppercase())).assertCountEquals(0)
    }

    private fun ComposeUiTest.showSettings(uiStyle: UiStyleOption) {
        setContent {
            AppTheme(themeConfig = ThemeConfig(uiStyle = uiStyle)) {
                SettingsScreen(
                    state = SettingsState(
                        themeMode = ThemeMode.System,
                        palette = ColorPaletteOption.Default,
                        font = FontOption.Default,
                        uiStyle = uiStyle
                    ),
                    onAction = {}
                )
            }
        }
    }

    private fun ComposeUiTest.assertEveryTitleIsAHeading() {
        listOf(
            Res.string.settings_theme,
            Res.string.settings_colors,
            Res.string.settings_font,
            Res.string.settings_ui_style,
            Res.string.settings_todos,
            Res.string.settings_backup,
            Res.string.settings_support
        ).forEach { title ->
            onNode(hasText(runBlocking { getString(title) }) and isHeading()).assertExists()
        }
    }
}
