package com.artemonre.onemoretodolist

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.artemonre.onemoretodolist.feature.todolist.di.webTodoDataModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(platformModules = listOf(webTodoDataModule()))
    }
}