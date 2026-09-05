package com.artemonre.onemoretodolist.core.container

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artemonre.onemoretodolist.feature.settings.navigation.settingsTab
import com.artemonre.onemoretodolist.feature.todolist.domain.SeedOnboardingTodos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContainerViewModel(
    contentTabs: List<NavigationTab>,
    private val seedOnboardingTodos: SeedOnboardingTodos
) : ViewModel() {

    private val _state = MutableStateFlow(ContainerState(tabs = contentTabs + settingsTab()))
    val state = _state.asStateFlow()

    fun onAction(action: ContainerAction) {
        when (action) {
            ContainerAction.OnStart -> viewModelScope.launch { seedOnboardingTodos() }
            is ContainerAction.OnTabSelected -> _state.update { it.copy(selectedTabIndex = action.index) }
        }
    }
}
