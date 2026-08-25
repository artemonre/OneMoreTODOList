package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.Room
import androidx.room3.RoomDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
    val appDataDir = File(System.getProperty("user.home"), ".onemoretodolist")
    appDataDir.mkdirs()
    val dbFile = File(appDataDir, "todo_items.db")
    return Room.databaseBuilder<AppDatabase>(name = dbFile.absolutePath)
}
