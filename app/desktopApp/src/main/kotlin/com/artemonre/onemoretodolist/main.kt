package com.artemonre.onemoretodolist

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.artemonre.onemoretodolist.feature.todolist.di.jvmTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab
import com.artemonre.onemoretodolist.observability.initSentry
import com.artemonre.onemoretodolist.observability.sentryDsn

fun main() = application {
    initSentry(sentryDsn, environment = "production")
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