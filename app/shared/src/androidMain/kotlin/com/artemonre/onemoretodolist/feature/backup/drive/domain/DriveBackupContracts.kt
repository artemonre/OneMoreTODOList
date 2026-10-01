package com.artemonre.onemoretodolist.feature.backup.drive.domain

import android.content.Intent
import android.content.IntentSender
import com.artemonre.onemoretodolist.core.domain.DataError
import com.artemonre.onemoretodolist.core.domain.EmptyResult
import com.artemonre.onemoretodolist.core.domain.Result
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow

// Google Drive backup is Android-only, and Google's consent step is an Android Activity round trip -
// so these contracts carry Android's own IntentSender/Intent rather than inventing opaque wrapper
// types the UI would have to cast back. They still depend on nothing in the data layer.

sealed interface DriveAuthorization {
    data class Granted(val accessToken: String) : DriveAuthorization
    // The user has to pick an account and/or consent - the UI launches this.
    data class NeedsConsent(val consentScreen: IntentSender) : DriveAuthorization
    // Google rejected the request before asking anyone: this build isn't registered for Drive
    // access (no OAuth client for its package name + signing key, or the Drive API is off).
    data object NotConfigured : DriveAuthorization
    data object Failed : DriveAuthorization
}

// Access to the hidden app-data folder in the user's Drive.
interface DriveAuthorizer {
    suspend fun authorize(): DriveAuthorization

    // The consent screen's result -> an access token, or null if the user backed out.
    fun tokenFromConsentResult(data: Intent?): String?
}

data class DriveBackupStatus(
    val isConnected: Boolean,
    val lastBackupAt: Instant?,
    val isAutoBackupEnabled: Boolean,
    val needsReconnect: Boolean
)

// How a backup attempt without the user (the daily background run) ended.
enum class BackgroundBackupOutcome {
    Done,
    // Couldn't get access without asking the user - flagged in status, nothing to retry.
    NeedsUser,
    // A temporary problem (network, server) - worth trying again later.
    Retry
}

interface DriveBackupRepository {
    val status: Flow<DriveBackupStatus>
    val isAutoBackupEnabled: Boolean
    suspend fun backup(token: String): EmptyResult<DataError.Remote>

    // Success(null) means this account has no backup yet.
    suspend fun download(token: String): Result<String?, DataError.Remote>
    suspend fun backupWithoutUser(): BackgroundBackupOutcome
    fun setAutoBackupEnabled(enabled: Boolean)

    // Revokes access (when a token is at hand) and forgets the connection. The backup file itself
    // stays in the user's Drive.
    suspend fun disconnect(token: String?)
}

// Runs backupWithoutUser() once a day while auto backup is on.
interface DriveAutoBackupScheduler {
    fun schedule()
    fun cancel()
}
