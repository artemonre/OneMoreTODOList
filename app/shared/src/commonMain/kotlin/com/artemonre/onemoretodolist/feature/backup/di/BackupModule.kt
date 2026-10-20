package com.artemonre.onemoretodolist.feature.backup.di

import com.artemonre.onemoretodolist.feature.backup.data.JsonTodoBackupCodec
import com.artemonre.onemoretodolist.feature.backup.domain.ExportTodos
import com.artemonre.onemoretodolist.feature.backup.domain.ImportTodos
import com.artemonre.onemoretodolist.feature.backup.domain.TodoBackupCodec
import org.koin.dsl.module

val backupModule = module {
    single<TodoBackupCodec> { JsonTodoBackupCodec() }
    single { ExportTodos(get(), get()) }
    single { ImportTodos(get(), get(), get()) }
}
