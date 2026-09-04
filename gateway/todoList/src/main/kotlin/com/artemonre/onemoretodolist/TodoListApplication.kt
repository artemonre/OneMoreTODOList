package com.artemonre.onemoretodolist

import android.app.Application
import com.artemonre.onemoretodolist.feature.todolist.di.androidTodoDataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Koin is otherwise only started lazily, the first time MainActivity composes App() - but a
// GlanceAppWidget/GlanceAppWidgetReceiver can run in a fresh process where that has never
// happened (e.g. a scheduled widget update, or the quick-add Activity, after the OS kills the
// app and later restarts just that entry point). Starting Koin here in Application.onCreate()
// guarantees it always exists before any component runs, Activity or widget alike.
class TodoListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TodoListApplication)
            modules(appKoinModules(listOf(androidTodoDataModule(this@TodoListApplication))))
        }
    }
}
