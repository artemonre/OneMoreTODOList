package com.artemonre.onemoretodolist.debug

import org.koin.core.module.Module
import org.koin.dsl.module

// Release stub - registers nothing, so app:shared's default no-op DebugConsentSettingsProvider
// binding stays in effect. See src/debug's real version.
fun debugAdsModule(): Module = module {}
