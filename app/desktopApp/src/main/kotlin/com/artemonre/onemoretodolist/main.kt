package com.artemonre.onemoretodolist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.artemonre.onemoretodolist.feature.todolist.di.jvmTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OneMoreTODOList",
    ) {
        App(
            platformModules = listOf(jvmTodoDataModule()),
            contentTabs = listOf(todoListTab())
        )
    }
}