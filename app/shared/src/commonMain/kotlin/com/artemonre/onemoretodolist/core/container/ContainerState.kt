package com.artemonre.onemoretodolist.core.container

data class ContainerState(
    val tabs: List<NavigationTab> = emptyList(),
    val selectedTabIndex: Int = 0
)
