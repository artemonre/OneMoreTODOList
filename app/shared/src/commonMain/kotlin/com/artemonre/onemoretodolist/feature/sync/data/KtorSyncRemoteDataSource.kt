package com.artemonre.onemoretodolist.feature.sync.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.core.network.safeCall
import com.artemonre.onemoretodolist.core.sync.SyncRequest
import com.artemonre.onemoretodolist.core.sync.SyncResponse
import com.artemonre.onemoretodolist.feature.backup.data.toTodoDto
import com.artemonre.onemoretodolist.feature.backup.data.toTodoItemOrNull
import com.artemonre.onemoretodolist.feature.sync.domain.SyncConfig
import com.artemonre.onemoretodolist.feature.sync.domain.SyncPage
import com.artemonre.onemoretodolist.feature.sync.domain.SyncRemoteDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

// POST {baseUrl}/sync - the contract is documented on SyncRequest in core. Maps between the
// domain's TodoItem and the wire's TodoDto here, so nothing above the data layer sees DTOs. A todo
// the server sends that this app version can't read is skipped, same as in a backup file.
class KtorSyncRemoteDataSource(
    private val httpClient: HttpClient,
    private val config: SyncConfig
) : SyncRemoteDataSource {
    override suspend fun sync(
        token: String,
        cursor: Long?,
        changes: List<TodoItem>
    ): Result<SyncPage, DataError.Remote> {
        val baseUrl = config.baseUrl ?: return Result.Error(DataError.Remote.UNKNOWN)
        val request = SyncRequest(cursor = cursor, changes = changes.map { it.toTodoDto() })
        return safeCall(
            request = {
                httpClient.post("${baseUrl.trimEnd('/')}/sync") {
                    bearerAuth(token)
                    contentType(ContentType.Application.Json)
                    setBody(request)
                }
            },
            onSuccess = { response ->
                val body = response.body<SyncResponse>()
                SyncPage(cursor = body.cursor, changes = body.changes.mapNotNull { it.toTodoItemOrNull() })
            }
        )
    }
}
