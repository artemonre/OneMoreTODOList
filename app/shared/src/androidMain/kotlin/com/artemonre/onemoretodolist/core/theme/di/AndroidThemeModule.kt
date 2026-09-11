package com.artemonre.onemoretodolist.core.theme.di

import com.artemonre.onemoretodolist.core.theme.data.NotifyingThemeRepository
import com.artemonre.onemoretodolist.core.theme.data.SettingsThemeRepository
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import org.koin.core.module.Module
import org.koin.dsl.module

// Overrides the common themeModule's ThemeRepository binding to hook a widget refresh onto every
// theme change - see TodoListApplication for the Android widget's use, and androidTodoDataModule
// for the equivalent hook on todo writes.
fun androidThemeModule(onThemeChanged: suspend () -> Unit = {}): Module = module {
    single<ThemeRepository> { NotifyingThemeRepository(SettingsThemeRepository(get()), onThemeChanged) }
}
