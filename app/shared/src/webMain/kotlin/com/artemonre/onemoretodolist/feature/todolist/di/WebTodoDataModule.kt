package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.data.InMemoryTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.TopSinceTrackingTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import org.koin.core.module.Module
import org.koin.dsl.module

fun webTodoDataModule(): Module = module {
    single<TodoLocalDataSource> { TopSinceTrackingTodoLocalDataSource(InMemoryTodoDataSource(), get<TodoPreferences>()) }
}
