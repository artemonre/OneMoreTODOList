package com.artemonre.onemoretodolist.feature.backup.drive.data

import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.domain.ExportTodos
import com.artemonre.onemoretodolist.feature.backup.drive.domain.BackgroundBackupOutcome
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorization
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveBackupRepository
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveBackupStatus
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getBooleanFlow
import com.russhwolf.settings.coroutines.getLongOrNullFlow
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

private const val KEY_CONNECTED = "drive_backup_connected"
private const val KEY_LAST_BACKUP_AT = "drive_backup_last_backup_at"
private const val KEY_AUTO_BACKUP = "drive_backup_auto_enabled"
private const val KEY_NEEDS_RECONNECT = "drive_backup_needs_reconnect"

// A repository in the proper sense: it combines the Drive remote (KtorDriveDataSource), the local
// todo list (via ExportTodos) and its own bits of state in settings. Tokens come from the caller
// (DriveBackupViewModel gets them with UI, the worker without); a token Drive rejects is cleared
// and retried once with a fresh one, since cached tokens expire after about an hour.
@OptIn(ExperimentalSettingsApi::class)
class GoogleDriveBackupRepository(
    private val driveApi: KtorDriveDataSource,
    private val authorizer: GoogleDriveAuthorizer,
    private val exportTodos: ExportTodos,
    private val settings: ObservableSettings,
    private val clock: Clock = Clock.System
) : DriveBackupRepository {
    override val status: Flow<DriveBackupStatus> = combine(
        settings.getBooleanFlow(KEY_CONNECTED, false),
        settings.getLongOrNullFlow(KEY_LAST_BACKUP_AT),
        settings.getBooleanFlow(KEY_AUTO_BACKUP, false),
        settings.getBooleanFlow(KEY_NEEDS_RECONNECT, false)
    ) { connected, lastBackupAt, autoBackup, needsReconnect ->
        DriveBackupStatus(
            isConnected = connected,
            lastBackupAt = lastBackupAt?.let(Instant::fromEpochMilliseconds),
            isAutoBackupEnabled = autoBackup,
            needsReconnect = needsReconnect
        )
    }

    override val isAutoBackupEnabled: Boolean get() = settings.getBoolean(KEY_AUTO_BACKUP, false)

    override suspend fun backup(token: String): EmptyResult<DataError.Remote> {
        val json = exportTodos()
        val result = withFreshTokenOnUnauthorized(token) { driveApi.uploadBackup(it, json) }
        if (result is Result.Success) {
            settings.putLong(KEY_LAST_BACKUP_AT, clock.now().toEpochMilliseconds())
            markConnected()
        }
        return result
    }

    override suspend fun download(token: String): Result<String?, DataError.Remote> {
        val result = withFreshTokenOnUnauthorized(token) { driveApi.downloadBackup(it) }
        if (result is Result.Success) markConnected()
        return result
    }

    override suspend fun backupWithoutUser(): BackgroundBackupOutcome {
        if (!isAutoBackupEnabled) return BackgroundBackupOutcome.Done
        val token = when (val authorization = authorizer.authorize()) {
            is DriveAuthorization.Granted -> authorization.accessToken
            is DriveAuthorization.NeedsConsent -> {
                settings.putBoolean(KEY_NEEDS_RECONNECT, true)
                return BackgroundBackupOutcome.NeedsUser
            }
            // Retrying won't help until the build's Cloud setup is fixed - nothing to do today.
            DriveAuthorization.NotConfigured -> return BackgroundBackupOutcome.Done
            DriveAuthorization.Failed -> return BackgroundBackupOutcome.Retry
        }
        return when (val result = backup(token)) {
            is Result.Success -> BackgroundBackupOutcome.Done
            is Result.Error -> when (result.error) {
                DataError.Remote.UNAUTHORIZED -> {
                    settings.putBoolean(KEY_NEEDS_RECONNECT, true)
                    BackgroundBackupOutcome.NeedsUser
                }
                else -> BackgroundBackupOutcome.Retry
            }
        }
    }

    override fun setAutoBackupEnabled(enabled: Boolean) {
        settings.putBoolean(KEY_AUTO_BACKUP, enabled)
    }

    override suspend fun disconnect(token: String?) {
        if (token != null) {
            driveApi.revokeAccess(token)
            authorizer.clearToken(token)
        }
        settings.remove(KEY_CONNECTED)
        settings.remove(KEY_LAST_BACKUP_AT)
        settings.remove(KEY_AUTO_BACKUP)
        settings.remove(KEY_NEEDS_RECONNECT)
    }

    private fun markConnected() {
        settings.putBoolean(KEY_CONNECTED, true)
        settings.putBoolean(KEY_NEEDS_RECONNECT, false)
    }

    private suspend fun <T> withFreshTokenOnUnauthorized(
        token: String,
        call: suspend (token: String) -> Result<T, DataError.Remote>
    ): Result<T, DataError.Remote> {
        val first = call(token)
        if (first !is Result.Error || first.error != DataError.Remote.UNAUTHORIZED) return first
        authorizer.clearToken(token)
        val fresh = authorizer.authorize() as? DriveAuthorization.Granted ?: return first
        return call(fresh.accessToken)
    }
}
