package com.artemonre.onemoretodolist.feature.todolist.di

import com.artemonre.onemoretodolist.feature.todolist.data.AppDatabase
import com.artemonre.onemoretodolist.feature.todolist.data.RoomTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.getDatabaseBuilder
import com.artemonre.onemoretodolist.feature.todolist.data.getRoomDatabase
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

fun iosTodoDataModule(): Module = module {
    // Dispatchers.IO doesn't exist on Kotlin/Native - Default runs on its own multithreaded pool.
    single { getRoomDatabase(getDatabaseBuilder(), Dispatchers.Default) }
    single { get<AppDatabase>().todoDao() }
    single<TodoLocalDataSource> { RoomTodoDataSource(get()) }
}
