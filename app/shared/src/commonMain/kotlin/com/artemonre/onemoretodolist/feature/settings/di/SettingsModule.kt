package com.artemonre.onemoretodolist.feature.settings.di

import com.artemonre.onemoretodolist.feature.settings.presentation.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {
    viewModelOf(::SettingsViewModel)
}
