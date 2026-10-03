package com.artemonre.onemoretodolist

import androidx.compose.runtime.Composable
import com.artemonre.onemoretodolist.core.container.ContainerRoot
import com.artemonre.onemoretodolist.core.container.NavigationTab
import com.artemonre.onemoretodolist.core.container.di.containerModule
import com.artemonre.onemoretodolist.core.designsystem.theme.AppTheme
import com.artemonre.onemoretodolist.core.network.di.networkModule
import com.artemonre.onemoretodolist.core.theme.data.themeModule
import com.artemonre.onemoretodolist.feature.backup.di.backupModule
import com.artemonre.onemoretodolist.feature.settings.di.settingsModule
import com.artemonre.onemoretodolist.feature.settings.navigation.settingsTab
import com.artemonre.onemoretodolist.feature.sync.di.syncModule
import com.artemonre.onemoretodolist.feature.todolist.di.todoListModule
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.koinConfiguration

// The single source of truth for this app's Koin module list - shared by App()'s lazy
// KoinApplication start and by any platform entry point (e.g. an Android Application subclass)
// that needs to start Koin eagerly before App() ever composes.
fun appKoinModules(platformModules: List<Module>): List<Module> =
    listOf(todoListModule, themeModule, settingsModule, containerModule, backupModule, networkModule, syncModule) + platformModules

@Composable
fun App(platformModules: List<Module>, contentTabs: List<NavigationTab>) {
    KoinApplication(
        configuration = koinConfiguration {
            modules(appKoinModules(platformModules))
        }
    ) {
        AppTheme {
            MandatoryAppUpdateGate {
                // Settings is a whole-app concern, always the last tab whatever the entry point
                // contributes - decided here at the composition root, not inside the container.
                ContainerRoot(tabs = contentTabs + settingsTab())
            }
        }
    }
}
