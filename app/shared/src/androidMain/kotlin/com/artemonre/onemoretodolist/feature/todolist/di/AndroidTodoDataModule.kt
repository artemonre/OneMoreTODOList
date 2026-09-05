package com.artemonre.onemoretodolist.feature.todolist.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.todolist.data.AppDatabase
import com.artemonre.onemoretodolist.feature.todolist.data.NotifyingTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.RoomTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.getDatabaseBuilder
import com.artemonre.onemoretodolist.feature.todolist.data.getRoomDatabase
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

// onDataChanged lets a gateway hook a widget refresh onto every write, without this Compose-only
// module needing to depend on Glance itself - see TodoListApplication for the Android widget's use.
fun androidTodoDataModule(context: Context, onDataChanged: suspend () -> Unit = {}): Module = module {
    single { getRoomDatabase(getDatabaseBuilder(context)) }
    single { get<AppDatabase>().todoDao() }
    single<TodoLocalDataSource> { NotifyingTodoLocalDataSource(RoomTodoDataSource(get()), onDataChanged) }
}
