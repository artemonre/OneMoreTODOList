package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.artemonre.onemoretodolist.core.designsystem.components.holo.HoloCounter
import com.artemonre.onemoretodolist.core.designsystem.components.material.MaterialCounter
import com.artemonre.onemoretodolist.core.designsystem.theme.LocalUiStyle
import com.artemonre.onemoretodolist.core.theme.domain.UiStyleOption

/**
 * A number with − and + buttons around it, for the current [LocalUiStyle]. The caller keeps the
 * value and its limits: turn a button off with [decreaseEnabled]/[increaseEnabled] at the edge.
 */
@Composable
fun AppCounter(
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    modifier: Modifier = Modifier,
    decreaseEnabled: Boolean = true,
    increaseEnabled: Boolean = true
) {
    when (LocalUiStyle.current) {
        UiStyleOption.Material -> MaterialCounter(
            value, onDecrease, onIncrease, decreaseDescription, increaseDescription, modifier, decreaseEnabled, increaseEnabled
        )
        UiStyleOption.Holo -> HoloCounter(
            value, onDecrease, onIncrease, decreaseDescription, increaseDescription, modifier, decreaseEnabled, increaseEnabled
        )
    }
}
