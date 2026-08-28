package com.artemonre.onemoretodolist.core.designsystem.components.material

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A stock Material3 [ModalBottomSheet] - default shape, colors, and scrim all from
 * `BottomSheetDefaults`, no customizations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = content
    )
}
