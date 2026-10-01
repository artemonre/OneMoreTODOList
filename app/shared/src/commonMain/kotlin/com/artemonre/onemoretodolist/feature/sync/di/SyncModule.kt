package com.artemonre.onemoretodolist.feature.sync.di

import com.artemonre.onemoretodolist.feature.sync.data.KtorSyncRemoteDataSource
import com.artemonre.onemoretodolist.feature.sync.data.SettingsSyncPreferences
import com.artemonre.onemoretodolist.feature.sync.domain.SyncAuthTokenProvider
import com.artemonre.onemoretodolist.feature.sync.domain.SyncConfig
import com.artemonre.onemoretodolist.feature.sync.domain.SyncPreferences
import com.artemonre.onemoretodolist.feature.sync.domain.SyncRemoteDataSource
import com.artemonre.onemoretodolist.feature.sync.domain.SyncTodos
import org.koin.dsl.module

// Client side of cloud sync. There's no server yet: baseUrl is null and nobody can sign in, so
// SyncTodos stops at NOT_CONFIGURED and nothing calls it. Wiring a server in later means filling in
// these two bindings (plus a sign-in flow behind the token provider).
val syncModule = module {
    single { SyncConfig(baseUrl = null) }
    single<SyncAuthTokenProvider> { SyncAuthTokenProvider { null } }
    single<SyncRemoteDataSource> { KtorSyncRemoteDataSource(get(), get()) }
    single<SyncPreferences> { SettingsSyncPreferences(get()) }
    single { SyncTodos(get(), get(), get(), get(), get(), get()) }
}
