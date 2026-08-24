package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.rounded.Check
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.IconSetOption

data class AppIcons(
    val checkIcon: ImageVector
)

private val DefaultAppIcons = AppIcons(checkIcon = Icons.Filled.Check)
private val RoundedAppIcons = AppIcons(checkIcon = Icons.Rounded.Check)

fun IconSetOption.toAppIcons(): AppIcons = when (this) {
    IconSetOption.Default -> DefaultAppIcons
    IconSetOption.Rounded -> RoundedAppIcons
}

val LocalAppIcons = staticCompositionLocalOf { DefaultAppIcons }

val LocalActionPlacement = staticCompositionLocalOf { ActionPlacement.End }
