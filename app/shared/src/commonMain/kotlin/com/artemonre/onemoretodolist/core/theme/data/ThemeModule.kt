package com.artemonre.onemoretodolist.core.theme.data

import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

// multiplatform-settings, not DataStore: this app has no Application class to hook DataStore's
// Android setup into, and DataStore needs custom expect/actual storage wiring for JS/WasmJs.
//
// Settings() isn't ObservableSettings on every target - JS/WasmJs's StorageSettings (backed by
// browser localStorage) only implements the plain, non-observable Settings interface. Android/iOS/
// JVM's implementations are natively observable, so only fall back to the callback-based
// makeObservable() wrapper where the native cast fails.
@OptIn(ExperimentalSettingsApi::class)
val themeModule = module {
    single<ObservableSettings> {
        val settings = Settings()
        settings as? ObservableSettings ?: settings.makeObservable()
    }
    singleOf(::SettingsThemeRepository) { bind<ThemeRepository>() }
}
