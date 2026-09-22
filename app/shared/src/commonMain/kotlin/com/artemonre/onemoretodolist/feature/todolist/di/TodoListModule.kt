package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.data.SettingsTodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.AddTodo
import com.artemonre.onemoretodolist.feature.todolist.domain.ApplyDueRecurrences
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.HandleDueTodoFired
import com.artemonre.onemoretodolist.feature.todolist.domain.NoOpDueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.ObserveActiveTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.RearmDueTodoAlarms
import com.artemonre.onemoretodolist.feature.todolist.domain.SeedOnboardingTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.ToggleTodoDone
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import com.artemonre.onemoretodolist.feature.todolist.domain.UpdateTopSince
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListViewModel
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val todoListModule = module {
    singleOf(::SeedOnboardingTodos)
    singleOf(::ApplyDueRecurrences)
    singleOf(::ObserveActiveTodos)
    singleOf(::AddTodo)
    singleOf(::ToggleTodoDone)
    singleOf(::UpdateTopSince)
    singleOf(::SettingsTodoPreferences) { bind<TodoPreferences>() }
    // Default no-op - overridden by androidDueTimeModule's real AlarmManager-backed scheduler on
    // Android, same override pattern androidTodoDataModule already uses for TodoPreferences.
    single<DueTimeScheduler> { NoOpDueTimeScheduler() }
    singleOf(::HandleDueTodoFired)
    singleOf(::RearmDueTodoAlarms)
    viewModelOf(::TodoListViewModel)
}
