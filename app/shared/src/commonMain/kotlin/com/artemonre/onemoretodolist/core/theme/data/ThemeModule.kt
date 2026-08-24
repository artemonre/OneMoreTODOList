package com.artemonre.onemoretodolist.core.theme.data

import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// multiplatform-settings, not DataStore: this app has no Application class to hook DataStore's
// Android setup into, and DataStore needs custom expect/actual storage wiring for JS/WasmJs.
val themeModule = module {
    single<ObservableSettings> { Settings() as ObservableSettings }
    singleOf(::SettingsThemeRepository) { bind<ThemeRepository>() }
}
