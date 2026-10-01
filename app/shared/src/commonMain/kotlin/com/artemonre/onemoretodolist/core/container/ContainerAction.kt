package com.artemonre.onemoretodolist.core.container

sealed interface ContainerAction {
    data object OnStart : ContainerAction
    data class OnTabSelected(val index: Int) : ContainerAction
}
