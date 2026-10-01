package com.artemonre.onemoretodolist.feature.sync.domain

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Error
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.domain.mergeTodos
import com.artemonre.onemoretodolist.feature.todolist.domain.DueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoLocalDataSource

// Where the sync server lives - null until there is one, which keeps sync switched off.
data class SyncConfig(val baseUrl: String?)

// The signed-in account's bearer token for the sync server, or null when signed out.
fun interface SyncAuthTokenProvider {
    suspend fun token(): String?
}

// What one sync round trip brings back: everything changed on the server since the given cursor,
// plus the cursor to send next time.
data class SyncPage(val cursor: Long, val changes: List<TodoItem>)

// Pushes `changes` and pulls everything changed since `cursor` (all of it when null). The wire
// format is the implementation's business, see KtorSyncRemoteDataSource.
interface SyncRemoteDataSource {
    suspend fun sync(token: String, cursor: Long?, changes: List<TodoItem>): Result<SyncPage, DataError.Remote>
}

// Where the last sync left off: the server's opaque cursor, and the newest updatedAt (epoch millis)
// this device has already pushed.
interface SyncPreferences {
    suspend fun cursor(): Long?
    suspend fun pushedUpTo(): Long?
    suspend fun save(cursor: Long, pushedUpTo: Long?)
}

enum class SyncError : Error {
    NOT_CONFIGURED,
    NOT_SIGNED_IN,
    NO_INTERNET,
    SERVER,
    STORAGE
}

data class SyncSummary(val pushedCount: Int, val pulledCount: Int)

/**
 * One sync round trip (the server's side is documented on core.sync.SyncRequest):
 * 1. push every local change newer than what was pushed last time - tombstones included;
 * 2. apply what the server sends back with "newest copy wins" (mergeTodos), re-reading local data
 *    first so edits made while the request was in flight aren't overwritten by older copies;
 * 3. only then remember the new cursor and push watermark - any failure leaves both untouched, so
 *    the next sync simply repeats this one.
 *
 * Copies pulled from the server keep their own updatedAt, which can be newer than the watermark, so
 * they get pushed back once next time - harmless, since the server ignores a copy that isn't newer
 * than its own.
 */
class SyncTodos(
    private val config: SyncConfig,
    private val tokenProvider: SyncAuthTokenProvider,
    private val remote: SyncRemoteDataSource,
    private val preferences: SyncPreferences,
    private val dataSource: TodoLocalDataSource,
    private val dueTimeScheduler: DueTimeScheduler
) {
    suspend operator fun invoke(): Result<SyncSummary, SyncError> {
        if (config.baseUrl == null) return Result.Error(SyncError.NOT_CONFIGURED)
        val token = tokenProvider.token() ?: return Result.Error(SyncError.NOT_SIGNED_IN)

        val pushedUpTo = preferences.pushedUpTo()
        val changes = dataSource.getAllIncludingDeleted()
            .filter { pushedUpTo == null || it.updatedAt.toEpochMilliseconds() > pushedUpTo }
        val page = when (val result = remote.sync(token, preferences.cursor(), changes)) {
            is Result.Success -> result.data
            is Result.Error -> return Result.Error(result.error.toSyncError())
        }

        val winners = mergeTodos(dataSource.getAllIncludingDeleted(), page.changes)
        if (winners.isNotEmpty()) {
            if (dataSource.upsertVerbatim(winners) is Result.Error) return Result.Error(SyncError.STORAGE)
            winners.forEach { todo ->
                if (todo.deletedAt != null) dueTimeScheduler.cancel(todo.id) else dueTimeScheduler.reschedule(todo)
            }
        }

        val newPushedUpTo = changes.maxOfOrNull { it.updatedAt.toEpochMilliseconds() } ?: pushedUpTo
        preferences.save(cursor = page.cursor, pushedUpTo = newPushedUpTo)
        return Result.Success(SyncSummary(pushedCount = changes.size, pulledCount = winners.size))
    }
}

private fun DataError.Remote.toSyncError(): SyncError = when (this) {
    DataError.Remote.NO_INTERNET -> SyncError.NO_INTERNET
    DataError.Remote.UNAUTHORIZED -> SyncError.NOT_SIGNED_IN
    DataError.Remote.SERVER_ERROR, DataError.Remote.SERIALIZATION, DataError.Remote.UNKNOWN -> SyncError.SERVER
}
