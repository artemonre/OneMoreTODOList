package com.artemonre.onemoretodolist.feature.settings.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig

// Placeholder destination for the Settings tab - editing ThemeConfig via ThemeRepository
// is a separate, future task.
@Composable
fun SettingsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Settings")
    }
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    AppTheme(themeConfig = ThemeConfig()) {
        SettingsScreen()
    }
}
