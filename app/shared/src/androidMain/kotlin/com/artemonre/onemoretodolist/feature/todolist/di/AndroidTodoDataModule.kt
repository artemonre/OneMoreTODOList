package com.artemonre.onemoretodolist.feature.todolist.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.todolist.data.AppDatabase
import com.artemonre.onemoretodolist.feature.todolist.data.NotifyingTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.RoomTodoDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.TopSinceTrackingTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.data.getDatabaseBuilder
import com.artemonre.onemoretodolist.feature.todolist.data.getRoomDatabase
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoPreferences
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

// onDataChanged lets a gateway hook a widget refresh onto every write, without this Compose-only
// module needing to depend on Glance itself - see TodoListApplication for the Android widget's use.
// TopSinceTrackingTodoLocalDataSource sits inside Notifying (closer to Room) so its own internal
// bookkeeping writes don't redundantly re-trigger onDataChanged on top of the write that caused them.
fun androidTodoDataModule(context: Context, onDataChanged: suspend () -> Unit = {}): Module = module {
    single { getRoomDatabase(getDatabaseBuilder(context), Dispatchers.IO) }
    single { get<AppDatabase>().todoDao() }
    single<TodoLocalDataSource> {
        NotifyingTodoLocalDataSource(
            TopSinceTrackingTodoLocalDataSource(RoomTodoDataSource(get()), get<TodoPreferences>()),
            onDataChanged
        )
    }
}
