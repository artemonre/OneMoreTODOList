package com.artemonre.onemoretodolist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.artemonre.onemoretodolist.feature.todolist.di.jvmTodoDataModule

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "OneMoreTODOList",
    ) {
        App(platformModules = listOf(jvmTodoDataModule()))
    }
}