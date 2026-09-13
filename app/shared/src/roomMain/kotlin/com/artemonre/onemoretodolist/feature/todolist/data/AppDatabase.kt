package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlin.coroutines.CoroutineContext

@Database(
    entities = [TodoEntity::class],
    version = 6,
    autoMigrations = [
        AutoMigration(from = 1, to = 2, spec = RenameTodoTitleToText::class),
        AutoMigration(from = 3, to = 4),
        // Adds the (nullable) recurrenceType/recurrenceInterval/recurrenceUnit columns - a plain
        // AutoMigration is enough since existing rows just get NULL (no recurrence).
        AutoMigration(from = 4, to = 5),
        // recurrenceAnchorDate (LocalDate, day precision) -> recurrenceAnchorInstant (Instant,
        // precise) - unreleased column, so a drop+add is fine, no data to preserve.
        AutoMigration(from = 5, to = 6, spec = DropRecurrenceAnchorDate::class)
    ]
)
@ColumnTypeConverters(TodoDateConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}

// Room's KSP compiler generates the actual for each target (android/iosArm64/
// iosSimulatorArm64/jvm) that compiles this source set - no actual is written by hand.
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

// Dispatchers.IO is JVM-only, so callers supply the right dispatcher for their target
// (Dispatchers.IO on android/jvm, Dispatchers.Default on iOS/Native) rather than this shared
// source set referencing one directly.
fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
    queryCoroutineContext: CoroutineContext
): AppDatabase {
    return builder
        .addMigrations(MIGRATION_2_3)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(queryCoroutineContext)
        .build()
}
