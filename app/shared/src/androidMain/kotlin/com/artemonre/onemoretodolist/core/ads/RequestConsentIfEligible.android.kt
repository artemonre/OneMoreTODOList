package com.artemonre.onemoretodolist.core.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import org.koin.compose.koinInject

@Composable
actual fun RequestConsentIfEligible() {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val adConsentState = koinInject<AdConsentState>()
    val interstitialAdController = koinInject<AndroidInterstitialAdController>()
    val tracker = koinInject<AndroidConsentEligibilityTracker>()
    val debugConsentSettingsProvider = koinInject<DebugConsentSettingsProvider>()

    LaunchedEffect(activity) {
        val currentActivity = activity ?: return@LaunchedEffect
        val consentInformation = UserMessagingPlatform.getConsentInformation(context)
        val debugSettings = debugConsentSettingsProvider.get(context)
        val params = ConsentRequestParameters.Builder()
            .apply { if (debugSettings != null) setConsentDebugSettings(debugSettings) }
            .build()

        fun onAdsAllowed() {
            adConsentState.allowAds()
            interstitialAdController.onAdsAllowed()
        }

        consentInformation.requestConsentInfoUpdate(
            currentActivity,
            params,
            {
                if (consentInformation.canRequestAds()) {
                    onAdsAllowed()
                } else if (tracker.hasPerformedTodoAction()) {
                    // Only reachable once the user has actually used the app, and only on a later
                    // app start than the one that action happened in - the flag was already true
                    // when this LaunchedEffect started, since an action can only be recorded while
                    // the app is already running.
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(currentActivity) {
                        if (consentInformation.canRequestAds()) onAdsAllowed()
                    }
                }
                // Else: not eligible yet (no qualifying todo action recorded) - stay silent, ads
                // stay blocked this session.
            },
            {
                // Couldn't determine consent status (e.g. no network) - fail safe by leaving ads
                // blocked rather than risk serving an EEA/UK/Switzerland user before consent is
                // actually resolved; retried fresh on the next app start.
            }
        )
    }
}
