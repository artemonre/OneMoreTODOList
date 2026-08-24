package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val todoListModule = module {
    viewModelOf(::TodoListViewModel)
}
