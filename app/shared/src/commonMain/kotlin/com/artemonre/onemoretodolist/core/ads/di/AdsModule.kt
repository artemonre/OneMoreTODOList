package com.artemonre.onemoretodolist.core.ads.di

import com.artemonre.onemoretodolist.core.ads.AdConfig
import org.koin.dsl.module

// Default test-ad-unit binding - overridden with real AdMob IDs by gateway/todoList's
// TodoListApplication, same override pattern androidDueTimeModule uses for DueTimeScheduler.
val adsModule = module {
    single { AdConfig() }
}
