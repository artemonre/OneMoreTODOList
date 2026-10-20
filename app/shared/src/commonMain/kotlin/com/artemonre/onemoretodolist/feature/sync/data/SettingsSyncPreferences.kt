package com.artemonre.onemoretodolist.feature.sync.data

import com.artemonre.onemoretodolist.feature.sync.domain.SyncPreferences
import com.russhwolf.settings.ObservableSettings

private const val KEY_CURSOR = "sync_cursor"
private const val KEY_PUSHED_UP_TO = "sync_pushed_up_to"

class SettingsSyncPreferences(private val settings: ObservableSettings) : SyncPreferences {
    override suspend fun cursor(): Long? = settings.getLongOrNull(KEY_CURSOR)

    override suspend fun pushedUpTo(): Long? = settings.getLongOrNull(KEY_PUSHED_UP_TO)

    override suspend fun save(cursor: Long, pushedUpTo: Long?) {
        settings.putLong(KEY_CURSOR, cursor)
        if (pushedUpTo != null) settings.putLong(KEY_PUSHED_UP_TO, pushedUpTo) else settings.remove(KEY_PUSHED_UP_TO)
    }
}
