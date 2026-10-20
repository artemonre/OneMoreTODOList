package com.artemonre.onemoretodolist.feature.backup.drive.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.core.network.safeCall
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.Serializable

private const val FILES_URL = "https://www.googleapis.com/drive/v3/files"
private const val UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files"
private const val REVOKE_URL = "https://oauth2.googleapis.com/revoke"
private const val APP_DATA_FOLDER = "appDataFolder"

// One fixed file, overwritten on every backup - the newest backup is the only one kept.
private const val BACKUP_FILE_NAME = "onemoretodolist-backup.json"

@Serializable
private data class DriveFileList(val files: List<DriveFile> = emptyList())

@Serializable
private data class DriveFile(val id: String)

// Drive REST v3 over Ktor, limited to the app-data folder (see GoogleDriveAuthorizer's scope). Only
// used inside the data layer (by GoogleDriveBackupRepository), so it has no domain interface.
class KtorDriveDataSource(private val httpClient: HttpClient) {

    suspend fun uploadBackup(token: String, json: String): EmptyResult<DataError.Remote> {
        val existingId = when (val found = findBackupId(token)) {
            is Result.Success -> found.data
            is Result.Error -> return found
        }
        return if (existingId == null) createBackup(token, json) else overwriteBackup(token, existingId, json)
    }

    // Success(null) means there's no backup yet.
    suspend fun downloadBackup(token: String): Result<String?, DataError.Remote> {
        val id = when (val found = findBackupId(token)) {
            is Result.Success -> found.data ?: return Result.Success(null)
            is Result.Error -> return found
        }
        return safeCall(
            request = {
                httpClient.get("$FILES_URL/$id") {
                    bearerAuth(token)
                    parameter("alt", "media")
                }
            },
            onSuccess = { it.bodyAsText() }
        )
    }

    // Revokes this app's access for the account the token belongs to - the Drive section's
    // "Disconnect". The backup file itself stays in Drive (the user can delete it from Drive's
    // "Manage apps" settings).
    suspend fun revokeAccess(token: String): EmptyResult<DataError.Remote> = safeCall(
        request = { httpClient.post(REVOKE_URL) { parameter("token", token) } },
        onSuccess = {}
    )

    private suspend fun findBackupId(token: String): Result<String?, DataError.Remote> = safeCall(
        request = {
            httpClient.get(FILES_URL) {
                bearerAuth(token)
                parameter("spaces", APP_DATA_FOLDER)
                parameter("q", "name = '$BACKUP_FILE_NAME' and trashed = false")
                parameter("fields", "files(id)")
            }
        },
        onSuccess = { it.body<DriveFileList>().files.firstOrNull()?.id }
    )

    // Drive's "multipart/related" upload - metadata (name + folder) and content in one request.
    // Ktor's MultiPartFormDataContent is form-data, a different format, so the body is built here.
    @OptIn(ExperimentalUuidApi::class)
    private suspend fun createBackup(token: String, json: String): EmptyResult<DataError.Remote> {
        val boundary = "backup-${Uuid.random()}"
        val metadata = """{"name":"$BACKUP_FILE_NAME","parents":["$APP_DATA_FOLDER"]}"""
        val body = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata)
            append("\r\n--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
            append(json)
            append("\r\n--$boundary--")
        }
        return safeCall(
            request = {
                httpClient.post(UPLOAD_URL) {
                    bearerAuth(token)
                    parameter("uploadType", "multipart")
                    setBody(TextContent(body, ContentType.parse("multipart/related; boundary=$boundary")))
                }
            },
            onSuccess = {}
        )
    }

    private suspend fun overwriteBackup(token: String, id: String, json: String): EmptyResult<DataError.Remote> =
        safeCall(
            request = {
                httpClient.patch("$UPLOAD_URL/$id") {
                    bearerAuth(token)
                    parameter("uploadType", "media")
                    setBody(TextContent(json, ContentType.Application.Json))
                }
            },
            onSuccess = {}
        )
}
