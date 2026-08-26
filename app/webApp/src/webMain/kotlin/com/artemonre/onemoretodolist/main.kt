package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.artemonre.onemoretodolist.feature.todolist.di.webTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(
            platformModules = listOf(webTodoDataModule()),
            contentTabs = listOf(todoListTab())
        )
    }
}