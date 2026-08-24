package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeMode
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import org.koin.compose.koinInject

@Composable
private fun rememberThemeConfig(): ThemeConfig {
    val repository = koinInject<ThemeRepository>()
    return repository.themeConfig.collectAsStateWithLifecycle(initialValue = ThemeConfig()).value
}

@Composable
fun AppTheme(
    themeConfig: ThemeConfig = rememberThemeConfig(),
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeConfig.mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val palette = themeConfig.palette.toColorPalette()
    val colorScheme = if (useDarkTheme) palette.dark else palette.light

    CompositionLocalProvider(
        LocalAppIcons provides themeConfig.iconSet.toAppIcons(),
        LocalActionPlacement provides themeConfig.actionPlacement
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = appTypography(themeConfig.font.toFontFamily())
        ) {
            AppBackground(style = themeConfig.background, content = content)
        }
    }
}
