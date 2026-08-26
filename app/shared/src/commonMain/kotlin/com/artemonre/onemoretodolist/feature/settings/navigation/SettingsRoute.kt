package com.artemonre.onemoretodolist.feature.settings.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface SettingsRoute : NavKey {
    @Serializable
    data object Main : SettingsRoute
}
