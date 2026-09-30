package com.artemonre.onemoretodolist.core.container

sealed interface ContainerAction {
    // onboardingTexts: the first-launch sample todos' text, already resolved in the current
    // language - only used if the list is still empty.
    data class OnStart(val onboardingTexts: List<String>) : ContainerAction
    data class OnTabSelected(val index: Int) : ContainerAction
}
