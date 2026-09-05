package com.artemonre.onemoretodolist.core.designsystem.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.vector.ImageVector
import com.artemonre.onemoretodolist.core.theme.domain.ActionPlacement
import com.artemonre.onemoretodolist.core.theme.domain.IconSetOption
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

data class AppIcons(
    val checkIcon: ImageVector,
    val addIcon: ImageVector,
    val scrollToTopIcon: ImageVector
)

private val DefaultAppIcons = AppIcons(
    checkIcon = Icons.Filled.Check,
    addIcon = Icons.Filled.Add,
    scrollToTopIcon = Icons.Filled.KeyboardArrowUp
)
private val RoundedAppIcons = AppIcons(
    checkIcon = Icons.Rounded.Check,
    addIcon = Icons.Rounded.Add,
    scrollToTopIcon = Icons.Rounded.KeyboardArrowUp
)

fun IconSetOption.toAppIcons(): AppIcons = when (this) {
    IconSetOption.Default -> DefaultAppIcons
    IconSetOption.Rounded -> RoundedAppIcons
}

val LocalAppIcons = staticCompositionLocalOf { DefaultAppIcons }

val LocalActionPlacement = staticCompositionLocalOf { ActionPlacement.End }

val LocalUiStyle = staticCompositionLocalOf { UiStyleOption.Material }
