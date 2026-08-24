package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface TodoListRoute : NavKey {
    @Serializable
    data object List : TodoListRoute
}
