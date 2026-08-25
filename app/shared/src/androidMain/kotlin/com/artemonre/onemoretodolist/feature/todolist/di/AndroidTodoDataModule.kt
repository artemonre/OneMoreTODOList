package com.artemonre.onemoretodolist.feature.todolist.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.todolist.data.AppDatabase
import com.artemonre.onemoretodolist.feature.todolist.data.RoomTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.getDatabaseBuilder
import com.artemonre.onemoretodolist.feature.todolist.data.getRoomDatabase
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import org.koin.core.module.Module
import org.koin.dsl.module

fun androidTodoDataModule(context: Context): Module = module {
    single { getRoomDatabase(getDatabaseBuilder(context)) }
    single { get<AppDatabase>().todoDao() }
    single<TodoLocalDataSource> { RoomTodoDataSource(get()) }
}
