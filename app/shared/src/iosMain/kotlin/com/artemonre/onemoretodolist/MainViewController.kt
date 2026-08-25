package com.artemonre.onemoretodolist

import androidx.compose.ui.window.ComposeUIViewController
import com.artemonre.onemoretodolist.feature.todolist.di.iosTodoDataModule

fun MainViewController() = ComposeUIViewController {
    App(platformModules = listOf(iosTodoDataModule()))
}