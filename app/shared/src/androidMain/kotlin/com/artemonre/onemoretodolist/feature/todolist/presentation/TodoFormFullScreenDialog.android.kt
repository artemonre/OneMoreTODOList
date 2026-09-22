package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

internal actual fun fullScreenDialogProperties(): DialogProperties =
    DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)

@Composable
internal actual fun ConfigureFullScreenDialogStatusBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    SideEffect {
        // A Dialog's content view is hosted by a DialogWindowProvider, which is the only way to
        // reach the dialog's own Window from Compose - it exposes no other API for this.
        val window = (view.parent as? DialogWindowProvider)?.window ?: return@SideEffect
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = darkIcons
        insetsController.isAppearanceLightNavigationBars = darkIcons
    }
}
