package com.artemonre.onemoretodolist.core.designsystem.components.holo

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.runComposeUiTest
import com.artemonre.onemoretodolist.core.designsystem.components.AppAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.components.AppStat
import com.artemonre.onemoretodolist.core.designsystem.components.AppStatsCard
import com.artemonre.onemoretodolist.core.designsystem.components.AppTagButton
import com.artemonre.onemoretodolist.core.designsystem.components.AppTagChip
import com.artemonre.onemoretodolist.core.designsystem.components.AppNavigationBar
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption
import kotlin.test.Test

// Holo shows text in capitals with decorative glyphs ("> COMPLETED TODOS", "# MONEY"). TalkBack
// must still get the text as written, without the glyphs.
@OptIn(ExperimentalTestApi::class)
class HoloAccessibilityTest {

    @Test
    fun `Holo reads text as written, without glyphs or capitals`() = runComposeUiTest {
        setContent {
            AppTheme(themeConfig = ThemeConfig(uiStyle = UiStyleOption.Holo), paintBackground = false) {
                Column {
                    AppStatsCard(
                        title = "Completed todos",
                        stats = listOf(AppStat("Today", 0), AppStat("This week", 1))
                    )
                    AppTagChip(text = "Money", containerColor = Color.White, contentColor = Color.Black)
                    AppTagButton(text = "Add tag", onClick = {}, leadingIcon = Icons.Filled.Add)
                    AppNavigationBar(
                        items = listOf("Todo", "Settings"),
                        selectedIndex = 0,
                        onItemSelected = {},
                        icon = { Icons.Filled.Add },
                        label = { it }
                    )
                }
            }
        }

        onNode(hasContentDescription("Completed todos") and isHeading()).assertExists()
        onNode(hasContentDescription("Today 0")).assertExists()
        onNode(hasContentDescription("This week 1")).assertExists()
        onNode(hasContentDescription("Money")).assertExists()
        onNode(hasContentDescription("Add tag") and hasClickAction()).assertExists()
        onNode(hasContentDescription("Settings")).assertExists()

        // The merged tree is what TalkBack reads.
        listOf(">", "#", "COMPLETED TODOS", "TODAY", "MONEY", "ADD TAG", "SETTINGS").forEach { shown ->
            onAllNodes(hasText(shown, substring = true)).assertCountEquals(0)
        }
    }

    @Test
    fun `Holo dialog title is a heading without its prompt`() = runComposeUiTest {
        setContent {
            AppTheme(themeConfig = ThemeConfig(uiStyle = UiStyleOption.Holo), paintBackground = false) {
                AppAlertDialog(
                    onDismissRequest = {},
                    title = { Text("Import todos") },
                    text = { Text("Merge or replace?") },
                    confirmButton = { TextButton(onClick = {}) { Text("Merge") } }
                )
            }
        }

        onNode(hasText("Import todos") and isHeading()).assertExists()
        onNode(hasText(">", substring = true)).assertDoesNotExist()
    }
}
