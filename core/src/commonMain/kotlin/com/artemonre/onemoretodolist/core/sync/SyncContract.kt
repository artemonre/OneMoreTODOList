package com.artemonre.onemoretodolist.core.sync

import kotlinx.serialization.Serializable

/**
 * One sync round trip: `POST {baseUrl}/sync`, `Authorization: Bearer <token>`, JSON body
 * [SyncRequest], JSON response [SyncResponse]. The server implements this; the app's
 * KtorSyncRemoteDataSource calls it.
 *
 * Server contract:
 * - Stores each change in [changes] unless it already holds a copy of that id with a newer or equal
 *   [TodoDto.updatedAt] ("newest copy wins" - tombstones included).
 * - Returns every todo of this account changed since [cursor] (all of them when it's null), as
 *   stored after applying this request, plus a new opaque cursor for the next call.
 */
@Serializable
data class SyncRequest(
    // Opaque - only ever a value a previous SyncResponse returned, or null on the first sync.
    val cursor: Long? = null,
    val changes: List<TodoDto> = emptyList()
)

@Serializable
data class SyncResponse(
    val cursor: Long,
    val changes: List<TodoDto> = emptyList()
)
