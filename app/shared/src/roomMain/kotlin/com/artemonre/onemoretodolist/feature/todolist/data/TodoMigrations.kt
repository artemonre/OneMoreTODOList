package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.DeleteColumn
import androidx.room3.RenameColumn
import androidx.room3.migration.AutoMigrationSpec
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@RenameColumn(tableName = "todo_items", fromColumnName = "title", toColumnName = "text")
class RenameTodoTitleToText : AutoMigrationSpec

// recurrenceAnchorDate (LocalDate) was replaced by recurrenceAnchorInstant (Instant, precise to
// the millisecond) - a genuine type change, not a rename, and this column never shipped to a real
// user, so dropping it (Room's own AutoMigration adds the new nullable column automatically) is
// simpler than trying to convert day-precision values into instants.
@DeleteColumn(tableName = "todo_items", columnName = "recurrenceAnchorDate")
class DropRecurrenceAnchorDate : AutoMigrationSpec

// Splits the single `date` column into `creationDate` and `lastEditDate`. This can't be an
// AutoMigration (@RenameColumn only handles a 1:1 rename) since it also introduces a genuinely
// new NOT NULL column that needs a per-row value, not a single fixed default - existing rows get
// `lastEditDate` backfilled from their old `date` value. Recreating the table (rather than
// ALTER TABLE ADD COLUMN ... DEFAULT ...) avoids leaving a stray column default in the on-disk
// schema that wouldn't match what Room expects from the entity, which is otherwise SQLite's only
// way to add a NOT NULL column to a table that already has rows.
internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override suspend fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            "CREATE TABLE IF NOT EXISTS `todo_items_new` (`id` TEXT NOT NULL, `text` TEXT NOT NULL, " +
                "`status` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `creationDate` INTEGER NOT NULL, " +
                "`lastEditDate` INTEGER NOT NULL, `priorityOrder` REAL, PRIMARY KEY(`id`))"
        )
        connection.execSQL(
            "INSERT INTO `todo_items_new` (`id`, `text`, `status`, `sortOrder`, `creationDate`, " +
                "`lastEditDate`, `priorityOrder`) " +
                "SELECT `id`, `text`, `status`, `sortOrder`, `date`, `date`, `priorityOrder` FROM `todo_items`"
        )
        connection.execSQL("DROP TABLE `todo_items`")
        connection.execSQL("ALTER TABLE `todo_items_new` RENAME TO `todo_items`")
    }
}
