package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.data.AppDatabase
import com.artemonre.onemoretodolist.feature.todolist.data.RoomTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.TopSinceTrackingTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.getDatabaseBuilder
import com.artemonre.onemoretodolist.feature.todolist.data.getRoomDatabase
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun jvmTodoDataModule(): Module = module {
    single { getRoomDatabase(getDatabaseBuilder(), Dispatchers.IO) }
    single { get<AppDatabase>().todoDao() }
    single<TodoLocalDataSource> { TopSinceTrackingTodoLocalDataSource(RoomTodoDataSource(get()), get<TodoPreferences>()) }
}
