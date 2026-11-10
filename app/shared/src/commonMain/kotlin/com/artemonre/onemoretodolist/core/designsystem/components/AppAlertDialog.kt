package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialog
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialAlertDialogDefaults
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * An alert dialog for the current [LocalUiStyle], falling back to [MaterialAlertDialog] for any
 * style without its own. See [MaterialAlertDialog] for the parameters.
 */
@Composable
fun AppAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (() -> Unit)? = null,
    icon: @Composable (() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = MaterialAlertDialogDefaults.ContentPadding,
    buttonsTopSpacing: Dp = MaterialAlertDialogDefaults.ButtonsTopSpacing,
    topDecoration: @Composable (() -> Unit)? = null,
    topDecorationOverhang: Dp = 0.dp,
    bottomDecoration: @Composable (() -> Unit)? = null,
    bottomDecorationHeight: Dp = 0.dp,
    properties: DialogProperties = DialogProperties()
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialAlertDialog(
            onDismissRequest, confirmButton, modifier, dismissButton, icon, title, text, contentPadding,
            buttonsTopSpacing, topDecoration, topDecorationOverhang, bottomDecoration, bottomDecorationHeight, properties
        )
        UiStyleOption.Holo -> HoloAlertDialog(
            onDismissRequest, confirmButton, modifier, dismissButton, icon, title, text, contentPadding,
            buttonsTopSpacing, topDecoration, topDecorationOverhang, bottomDecoration, bottomDecorationHeight, properties
        )
    }
}
