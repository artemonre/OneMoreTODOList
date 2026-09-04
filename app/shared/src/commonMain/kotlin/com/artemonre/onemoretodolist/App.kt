package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.core.container.ContainerRoot
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.container.di.containerModule
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.theme.data.themeModule
import com.artemonre.onemoretodolist.feature.settings.di.settingsModule
import com.artemonre.onemoretodolist.feature.todolist.di.todoListModule
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

// The single source of truth for this app's Koin module list - shared by App()'s lazy
// KoinApplication start and by any platform entry point (e.g. an Android Application subclass)
// that needs to start Koin eagerly before App() ever composes.
fun appKoinModules(platformModules: List<Module>): List<Module> =
    listOf(todoListModule, themeModule, settingsModule, containerModule) + platformModules

@Composable
fun App(platformModules: List<Module>, contentTabs: List<NavigationTab>) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appKoinModules(platformModules))
        }
    ) {
        AppTheme {
            ContainerRoot(contentTabs = contentTabs)
        }
    }
}
