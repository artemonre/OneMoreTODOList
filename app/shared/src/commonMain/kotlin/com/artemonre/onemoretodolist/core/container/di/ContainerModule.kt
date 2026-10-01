package com.artemonre.onemoretodolist.core.container.di

import com.artemonre.onemoretodolist.core.container.ContainerViewModel
import com.artemonre.onemoretodolist.core.domain.AppStartTask
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val containerModule = module {
    viewModel { params ->
        ContainerViewModel(tabs = params.get(), startTasks = getAll<AppStartTask>())
    }
}
