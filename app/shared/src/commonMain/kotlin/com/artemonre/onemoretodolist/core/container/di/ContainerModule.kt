package com.artemonre.onemoretodolist.core.container.di

import com.artemonre.onemoretodolist.core.container.ContainerViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val containerModule = module {
    viewModel { params -> ContainerViewModel(contentTabs = params.get(), seedOnboardingTodos = get()) }
}
