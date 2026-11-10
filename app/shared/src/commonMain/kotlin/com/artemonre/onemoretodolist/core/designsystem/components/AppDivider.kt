package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloDivider
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A thin horizontal line between rows, drawn in every style - Material3's [HorizontalDivider] by
 * default. Unlike [AppSectionDivider], which only some styles draw.
 */
@Composable
fun AppDivider(modifier: Modifier = Modifier) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> HorizontalDivider(modifier = modifier)
        UiStyleOption.Holo -> HoloDivider(modifier = modifier)
    }
}
