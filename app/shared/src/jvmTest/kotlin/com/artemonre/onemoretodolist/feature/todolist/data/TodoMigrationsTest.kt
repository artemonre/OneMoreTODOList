package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class TodoMigrationsTest {

    @Test
    fun `MIGRATION_2_3 splits date into creationDate and lastEditDate`() = runTest {
        val driver = BundledSQLiteDriver()
        val connection = driver.open(":memory:")
        try {
            connection.execSQL(
                "CREATE TABLE `todo_items` (`id` TEXT NOT NULL, `text` TEXT NOT NULL, " +
                    "`status` TEXT NOT NULL, `sortOrder` INTEGER NOT NULL, `date` INTEGER NOT NULL, " +
                    "`priorityOrder` REAL, PRIMARY KEY(`id`))"
            )
            connection.execSQL(
                "INSERT INTO `todo_items` (`id`, `text`, `status`, `sortOrder`, `date`, `priorityOrder`) " +
                    "VALUES ('1', 'Existing todo', 'Active', 0, 19000, NULL)"
            )

            MIGRATION_2_3.migrate(connection)

            val statement = connection.prepare(
                "SELECT `id`, `text`, `status`, `sortOrder`, `creationDate`, `lastEditDate`, `priorityOrder` " +
                    "FROM `todo_items`"
            )
            try {
                assertTrue(statement.step())
                assertEquals("1", statement.getText(0))
                assertEquals("Existing todo", statement.getText(1))
                assertEquals("Active", statement.getText(2))
                assertEquals(0L, statement.getLong(3))
                assertEquals(19000L, statement.getLong(4))
                assertEquals(19000L, statement.getLong(5))
                assertTrue(statement.isNull(6))
                assertTrue(!statement.step())
            } finally {
                statement.close()
            }
        } finally {
            connection.close()
        }
    }
}
