package com.artemonre.onemoretodolist.feature.sync

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.core.network.createHttpClient
import com.artemonre.onemoretodolist.core.sync.BackupJson
import com.artemonre.onemoretodolist.core.sync.TodoDto
import com.artemonre.onemoretodolist.feature.backup.data.toTodoDto
import com.artemonre.onemoretodolist.feature.sync.data.KtorSyncRemoteDataSource
import com.artemonre.onemoretodolist.feature.sync.domain.SyncConfig
import com.artemonre.onemoretodolist.feature.sync.domain.SyncError
import com.artemonre.onemoretodolist.feature.sync.domain.SyncPage
import com.artemonre.onemoretodolist.feature.sync.domain.SyncPreferences
import com.artemonre.onemoretodolist.feature.sync.domain.SyncRemoteDataSource
import com.artemonre.onemoretodolist.feature.sync.domain.SyncSummary
import com.artemonre.onemoretodolist.feature.sync.domain.SyncTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.NoOpDueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate

class SyncTodosTest {

    @Test
    fun `pushes only changes newer than the last push and remembers the new cursor and watermark`() = runTest {
        val dataSource = FakeTodoLocalDataSource(listOf(todo("old", updatedAt = 1_000), todo("new", updatedAt = 3_000)))
        val preferences = FakeSyncPreferences(cursor = 7, pushedUpTo = 2_000)
        val remote = FakeSyncRemote(SyncPage(cursor = 8, changes = emptyList()))

        val result = syncTodos(dataSource, remote, preferences)()

        assertEquals(Result.Success(SyncSummary(pushedCount = 1, pulledCount = 0)), result)
        assertEquals(7L, remote.lastCursor)
        assertEquals(listOf(todo("new", updatedAt = 3_000)), remote.lastChanges)
        assertEquals(8L, preferences.cursor)
        assertEquals(3_000L, preferences.pushedUpTo)
    }

    @Test
    fun `applies newer remote copies and remote deletions, ignores older ones`() = runTest {
        val dataSource = FakeTodoLocalDataSource(
            listOf(todo("a", "Local A", 2_000), todo("b", "Local B", 2_000), todo("c", "Local C", 2_000))
        )
        val remote = FakeSyncRemote(
            SyncPage(
                cursor = 1,
                changes = listOf(
                    todo("a", "Remote A", 3_000),
                    todo("b", "Remote B", 1_000),
                    todo("c", "Remote C", 3_000).copy(deletedAt = Instant.fromEpochMilliseconds(3_000))
                )
            )
        )

        val result = syncTodos(dataSource, remote, FakeSyncPreferences())()

        assertEquals(2, (result as Result.Success).data.pulledCount)
        assertEquals(listOf("Remote A", "Local B"), dataSource.observeTodos().first().map { it.text })
    }

    @Test
    fun `a failed request leaves local data and the cursor untouched`() = runTest {
        val dataSource = FakeTodoLocalDataSource(listOf(todo("a", updatedAt = 1_000)))
        val preferences = FakeSyncPreferences(cursor = 5, pushedUpTo = null)

        val result = syncTodos(dataSource, FakeSyncRemote(error = DataError.Remote.NO_INTERNET), preferences)()

        assertEquals(Result.Error(SyncError.NO_INTERNET), result)
        assertEquals(5L, preferences.cursor)
        assertEquals(null, preferences.pushedUpTo)
        assertEquals(listOf("a"), dataSource.observeTodos().first().map { it.id })
    }

    @Test
    fun `without a server or a sign-in nothing is sent`() = runTest {
        val remote = FakeSyncRemote(SyncPage(cursor = 1, changes = emptyList()))
        val dataSource = FakeTodoLocalDataSource()

        val notConfigured = SyncTodos(SyncConfig(null), { "token" }, remote, FakeSyncPreferences(), dataSource, NoOpDueTimeScheduler())()
        val signedOut = SyncTodos(SyncConfig("https://example.test"), { null }, remote, FakeSyncPreferences(), dataSource, NoOpDueTimeScheduler())()

        assertEquals(Result.Error(SyncError.NOT_CONFIGURED), notConfigured)
        assertEquals(Result.Error(SyncError.NOT_SIGNED_IN), signedOut)
        assertEquals(null, remote.lastCursor)
        assertEquals(null, remote.lastChanges)
    }

    @Test
    fun `the Ktor client posts the request with the token and maps the response to todos`() = runTest {
        var seenUrl: String? = null
        var seenAuth: String? = null
        val engine = MockEngine { request ->
            seenUrl = request.url.toString()
            seenAuth = request.headers[HttpHeaders.Authorization]
            respond(
                content = """{"cursor": 42, "changes": [${BackupJson.encodeToString(TodoDto.serializer(), todo("r", updatedAt = 5_000).toTodoDto())}]}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = createHttpClient(engine)

        val result = KtorSyncRemoteDataSource(client, SyncConfig("https://example.test/api/")).sync("secret", null, emptyList())

        assertEquals(Result.Success(SyncPage(cursor = 42, changes = listOf(todo("r", updatedAt = 5_000)))), result)
        assertEquals("https://example.test/api/sync", seenUrl)
        assertEquals("Bearer secret", seenAuth)
    }

    @Test
    fun `the Ktor client maps 401 to UNAUTHORIZED`() = runTest {
        val client = createHttpClient(MockEngine { respond(content = "", status = HttpStatusCode.Unauthorized) })

        val result = KtorSyncRemoteDataSource(client, SyncConfig("https://example.test")).sync("expired", null, emptyList())

        assertEquals(Result.Error(DataError.Remote.UNAUTHORIZED), result)
    }

    private fun syncTodos(
        dataSource: FakeTodoLocalDataSource,
        remote: SyncRemoteDataSource,
        preferences: SyncPreferences
    ) = SyncTodos(SyncConfig("https://example.test"), { "token" }, remote, preferences, dataSource, NoOpDueTimeScheduler())

    private fun todo(id: String, text: String = "Todo $id", updatedAt: Long) = TodoItem(
        id = id,
        text = text,
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        updatedAt = Instant.fromEpochMilliseconds(updatedAt)
    )
}

private class FakeSyncRemote(
    private val page: SyncPage? = null,
    private val error: DataError.Remote? = null
) : SyncRemoteDataSource {
    var lastCursor: Long? = null
    var lastChanges: List<TodoItem>? = null

    override suspend fun sync(token: String, cursor: Long?, changes: List<TodoItem>): Result<SyncPage, DataError.Remote> {
        lastCursor = cursor
        lastChanges = changes
        return if (error != null) Result.Error(error) else Result.Success(requireNotNull(page))
    }
}

private class FakeSyncPreferences(var cursor: Long? = null, var pushedUpTo: Long? = null) : SyncPreferences {
    override suspend fun cursor(): Long? = cursor
    override suspend fun pushedUpTo(): Long? = pushedUpTo
    override suspend fun save(cursor: Long, pushedUpTo: Long?) {
        this.cursor = cursor
        this.pushedUpTo = pushedUpTo
    }
}
