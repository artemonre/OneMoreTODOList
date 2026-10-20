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

class TodoAutoMigration10To11Test {

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
    fun `10 to 11 backfills createdAt from creationDate plus sortOrder`() = runTest {
        // 20000 epoch days = 2024-10-04.
        helper.createDatabase(10).use { connection ->
            connection.execSQL(
                "INSERT INTO `todo_items` (`id`, `text`, `status`, `sortOrder`, `creationDate`, `lastEditDate`, `updatedAt`) " +
                    "VALUES ('1', 'First', 'Active', -1, 20000, 20000, 0), ('2', 'Second', 'Active', 3, 20000, 20000, 0)"
            )
        }

        helper.runMigrationsAndValidate(11).use { connection ->
            connection.prepare("SELECT `id`, `text`, `createdAt` FROM `todo_items` ORDER BY `id`").use { statement ->
                assertTrue(statement.step())
                assertEquals("1", statement.getText(0))
                assertEquals("First", statement.getText(1))
                assertEquals(20000L * 86_400_000L - 1, statement.getLong(2))
                assertTrue(statement.step())
                assertEquals("2", statement.getText(0))
                assertEquals(20000L * 86_400_000L + 3, statement.getLong(2))
                assertTrue(!statement.step())
            }
        }
    }
}
