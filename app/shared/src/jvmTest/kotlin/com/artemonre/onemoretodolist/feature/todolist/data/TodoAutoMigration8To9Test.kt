package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class TodoAutoMigration8To9Test {

    private val databasePath: Path = Files.createTempDirectory("todo-migration").resolve("test.db")

    @get:Rule
    val helper = MigrationTestHelper(
        schemaDirectoryPath = Path("schemas"),
        databasePath = databasePath,
        driver = BundledSQLiteDriver(),
        databaseClass = AppDatabase::class,
        databaseFactory = { AppDatabaseConstructor.initialize() }
    )

    @Test
    fun `8 to 9 keeps existing todos and gives them no snooze, checklist, or tags`() = runTest {
        helper.createDatabase(8).use { connection ->
            connection.execSQL(
                "INSERT INTO `todo_items` (`id`, `text`, `status`, `sortOrder`, `creationDate`, `lastEditDate`) " +
                    "VALUES ('1', 'Existing todo', 'Active', 0, 19000, 19000)"
            )
        }

        helper.runMigrationsAndValidate(9).use { connection ->
            connection.prepare("SELECT `id`, `text`, `snoozedUntil`, `checklist`, `tags` FROM `todo_items`").use { statement ->
                assertTrue(statement.step())
                assertEquals("1", statement.getText(0))
                assertEquals("Existing todo", statement.getText(1))
                assertTrue(statement.isNull(2))
                assertEquals("[]", statement.getText(3))
                assertEquals("[]", statement.getText(4))
                assertTrue(!statement.step())
            }
        }
    }
}
