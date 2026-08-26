package com.artemonre.onemoretodolist

import androidx.compose.ui.window.ComposeUIViewController
import com.artemonre.onemoretodolist.feature.todolist.di.iosTodoDataModule
import com.artemonre.onemoretodolist.feature.todolist.navigation.todoListTab

fun MainViewController() = ComposeUIViewController {
    App(
        platformModules = listOf(iosTodoDataModule()),
        contentTabs = listOf(todoListTab())
    )
}