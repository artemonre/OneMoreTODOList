package com.artemonre.onemoretodolist.core.theme.data

import com.artemonre.onemoretodolist.core.theme.domain.ThemeConfig
import com.artemonre.onemoretodolist.core.theme.domain.ThemeRepository
import kotlinx.coroutines.flow.Flow

// Wraps a ThemeRepository, invoking onThemeChanged after every update() - used on Android to push
// an immediate Glance widget refresh when the theme (e.g. color palette) changes, mirroring
// NotifyingTodoLocalDataSource's widget-refresh hook for todo writes.
class NotifyingThemeRepository(
    private val delegate: ThemeRepository,
    private val onThemeChanged: suspend () -> Unit
) : ThemeRepository {
    override val themeConfig: Flow<ThemeConfig> = delegate.themeConfig

    override suspend fun update(transform: (ThemeConfig) -> ThemeConfig) {
        delegate.update(transform)
        onThemeChanged()
    }
}
