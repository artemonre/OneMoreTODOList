package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

// Status the app reports back while or after something happens: snackbars and progress bars.

/** Shows [hostState]'s snackbars for the current [LocalUiStyle] - Material3's [SnackbarHost] by default. */
@Composable
fun AppSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    when (LocalUiStyle.current) {
        // No Holo snackbar yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> SnackbarHost(hostState = hostState, modifier = modifier)
    }
}

/** An indeterminate progress bar for the current [LocalUiStyle] - Material3's [LinearProgressIndicator] by default. */
@Composable
fun AppLinearProgressIndicator(modifier: Modifier = Modifier) {
    when (LocalUiStyle.current) {
        // No Holo progress bar yet - fall back to Material.
        UiStyleOption.Material, UiStyleOption.Holo -> LinearProgressIndicator(modifier = modifier)
    }
}
