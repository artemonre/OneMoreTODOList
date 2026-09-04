package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.ObserveActiveTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.SeedOnboardingTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val todoListModule = module {
    singleOf(::SeedOnboardingTodos)
    singleOf(::ObserveActiveTodos)
    singleOf(::AddTodo)
    singleOf(::ToggleTodoDone)
    viewModelOf(::TodoListViewModel)
}
