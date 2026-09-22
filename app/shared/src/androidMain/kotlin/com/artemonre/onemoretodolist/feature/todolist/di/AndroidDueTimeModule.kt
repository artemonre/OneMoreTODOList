package com.artemonre.onemoretodolist.feature.todolist.di

import android.content.Context
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.notification.AndroidAlarmDueTimeScheduler
import org.koin.core.module.Module
import org.koin.dsl.module

// Overrides the common todoListModule's no-op DueTimeScheduler binding with the real
// AlarmManager-backed one - same override pattern androidTodoDataModule already uses for
// TodoPreferences.
fun androidDueTimeModule(context: Context): Module = module {
    single<DueTimeScheduler> { AndroidAlarmDueTimeScheduler(context) }
}
