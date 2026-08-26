package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.core.container.ContainerRoot
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.container.di.containerModule
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.data.themeModule
import com.artemonre.onemoretodolist.feature.todolist.di.todoListModule
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

@Composable
fun App(platformModules: List<Module>, contentTabs: List<NavigationTab>) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(todoListModule, themeModule, containerModule, *platformModules.toTypedArray())
        }
    ) {
        AppTheme {
            ContainerRoot(contentTabs = contentTabs)
        }
    }
}
