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

class TodoAutoMigration9To10Test {

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
    fun `9 to 10 backfills updatedAt from lastEditDate and leaves todos undeleted`() = runTest {
        // 20000 epoch days = 2024-10-04.
        helper.createDatabase(9).use { connection ->
            connection.execSQL(
                "INSERT INTO `todo_items` (`id`, `text`, `status`, `sortOrder`, `creationDate`, `lastEditDate`) " +
                    "VALUES ('1', 'Existing todo', 'Active', 0, 19000, 20000)"
            )
        }

        helper.runMigrationsAndValidate(10).use { connection ->
            connection.prepare("SELECT `id`, `text`, `updatedAt`, `deletedAt` FROM `todo_items`").use { statement ->
                assertTrue(statement.step())
                assertEquals("1", statement.getText(0))
                assertEquals("Existing todo", statement.getText(1))
                assertEquals(20000L * 86_400_000L, statement.getLong(2))
                assertTrue(statement.isNull(3))
                assertTrue(!statement.step())
            }
        }
    }
}
