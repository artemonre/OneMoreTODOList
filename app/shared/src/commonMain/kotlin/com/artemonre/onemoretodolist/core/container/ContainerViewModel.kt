package com.artemonre.onemoretodolist.core.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.AnalyticsEvents
import com.artemonre.onemoretodolist.analytics.NoOpAnalytics
import com.artemonre.onemoretodolist.core.domain.AppStartTask
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// tabs: every tab in the bottom bar, in order - assembled by the composition root (App), so the
// container itself knows no feature. startTasks: what features want run on every start/foreground.
class ContainerViewModel(
    tabs: List<NavigationTab>,
    private val startTasks: List<AppStartTask>,
    private val analytics: Analytics = NoOpAnalytics
) : ViewModel() {

    private val _state = MutableStateFlow(ContainerState(tabs = tabs))
    val state = _state.asStateFlow()

    fun onAction(action: ContainerAction) {
        when (action) {
            // One after another, in registration order - a later task may rely on an earlier one
            // (e.g. seeding before anything else touches the list).
            is ContainerAction.OnStart -> viewModelScope.launch {
                startTasks.forEach { it.run() }
            }
            is ContainerAction.OnTabSelected -> {
                val current = _state.value
                if (action.index != current.selectedTabIndex) {
                    current.tabs.getOrNull(action.index)?.let { tab ->
                        analytics.capture(AnalyticsEvents.tabSelected(tab.label.key.removePrefix("tab_")))
                    }
                }
                _state.update { it.copy(selectedTabIndex = action.index) }
            }
        }
    }
}
