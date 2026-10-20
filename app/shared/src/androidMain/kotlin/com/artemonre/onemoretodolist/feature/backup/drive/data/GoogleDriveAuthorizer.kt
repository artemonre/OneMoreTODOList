package com.artemonre.onemoretodolist.feature.backup.drive.data

import android.content.Context
import android.content.Intent
import android.util.Log
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorization
import com.artemonre.onemoretodolist.feature.backup.drive.domain.DriveAuthorizer
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.ClearTokenRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

// Only the hidden per-app folder - the app can't see or touch any of the user's own Drive files.
private const val DRIVE_APPDATA_SCOPE = "https://www.googleapis.com/auth/drive.appdata"
private const val LOG_TAG = "DriveBackup"

// Google Identity's AuthorizationClient - asks for the drive.appdata scope directly, no separate
// sign-in step. Once granted, authorize() hands back a fresh access token silently (which is how
// the daily background backup works) until the user revokes access.
class GoogleDriveAuthorizer(context: Context) : DriveAuthorizer {
    private val client = Identity.getAuthorizationClient(context)
    private val request = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(DRIVE_APPDATA_SCOPE)))
        .build()

    override suspend fun authorize(): DriveAuthorization {
        val result = try {
            client.authorize(request).await()
        } catch (e: ApiException) {
            // Logged with the status code - "10" (DEVELOPER_ERROR) is the usual Cloud-setup problem.
            Log.w(LOG_TAG, "Drive authorization failed, status ${e.statusCode}", e)
            return if (e.statusCode == CommonStatusCodes.DEVELOPER_ERROR) {
                DriveAuthorization.NotConfigured
            } else {
                DriveAuthorization.Failed
            }
        }
        val pendingIntent = result.pendingIntent
        val token = result.accessToken
        return when {
            result.hasResolution() && pendingIntent != null -> DriveAuthorization.NeedsConsent(pendingIntent.intentSender)
            token != null -> DriveAuthorization.Granted(token)
            else -> DriveAuthorization.Failed
        }
    }

    override fun tokenFromConsentResult(data: Intent?): String? {
        return try {
            client.getAuthorizationResultFromIntent(data).accessToken
        } catch (e: ApiException) {
            null
        }
    }

    // Drops a token Google rejected (expired/revoked) from the local cache, so the next
    // authorize() fetches a fresh one instead of handing the same one back.
    suspend fun clearToken(token: String) {
        try {
            client.clearToken(ClearTokenRequest.builder().setToken(token).build()).await()
        } catch (e: ApiException) {
            // Nothing cached to clear - the next authorize() fetches a new token either way.
        }
    }
}

private suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
    addOnCanceledListener { continuation.cancel() }
}
