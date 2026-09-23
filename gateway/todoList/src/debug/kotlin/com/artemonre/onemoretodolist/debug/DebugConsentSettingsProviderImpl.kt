package com.artemonre.onemoretodolist.debug

import android.content.Context
import android.provider.Settings
import android.util.Log
import com.artemonre.onemoretodolist.core.ads.DebugConsentSettingsProvider
import com.google.android.ump.ConsentDebugSettings
import java.security.MessageDigest

private const val LOG_TAG = "ConsentDebug"

// Real implementation of app:shared's DebugConsentSettingsProvider hook - only ever registered in
// a debug build, see debugAdsModule.
class DebugConsentSettingsProviderImpl : DebugConsentSettingsProvider {
    override fun get(context: Context): ConsentDebugSettings? {
        if (!DebugPreferences(context).isEeaUkGeographyEnabled) return null
        val hashedId = testDeviceHashedId(context)
        // Compare this against the hash the SDK itself logs (search Logcat for
        // "addTestDeviceHashedId") if forced EEA still doesn't seem to be taking effect - they
        // should be identical 32-character uppercase hex strings.
        Log.d(LOG_TAG, "Debug menu EEA/UK geography enabled, computed test device hashed ID: $hashedId")
        return ConsentDebugSettings.Builder(context)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .addTestDeviceHashedId(hashedId)
            .build()
    }
}

// Google requires a registered test device alongside setDebugGeography, but never publishes this
// as a computable API - it's an unofficial (though long-stable and widely relied on) fact that the
// SDK's own test-device check is simply the uppercase hex MD5 of ANDROID_ID, the same value the
// SDK itself logs the first time it sees an unregistered device. Computing it here avoids a manual
// copy-from-Logcat step.
private fun testDeviceHashedId(context: Context): String {
    val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    val digest = MessageDigest.getInstance("MD5").digest(androidId.toByteArray())
    // it.toInt() and 0xFF masks off the sign extension - a negative (high-bit-set) Byte otherwise
    // formats as 8 hex digits instead of 2 (Java's %X sign-extends a Byte to Int before formatting),
    // garbling roughly half the output and producing a hash that never matches Google's own.
    return digest.joinToString("") { "%02X".format(it.toInt() and 0xFF) }
}
