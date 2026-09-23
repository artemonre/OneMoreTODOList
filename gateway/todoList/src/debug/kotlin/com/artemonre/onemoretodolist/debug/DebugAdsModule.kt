package com.artemonre.onemoretodolist.debug

import com.artemonre.onemoretodolist.core.ads.DebugConsentSettingsProvider
import org.koin.core.module.Module
import org.koin.dsl.module

// Real override - registered after (so it wins over) app:shared's default no-op binding. Paired
// with a same-named, empty stub in src/release - TodoListApplication calls this unconditionally,
// same trick as DebugMenuGate.
fun debugAdsModule(): Module = module {
    single<DebugConsentSettingsProvider> { DebugConsentSettingsProviderImpl() }
}
