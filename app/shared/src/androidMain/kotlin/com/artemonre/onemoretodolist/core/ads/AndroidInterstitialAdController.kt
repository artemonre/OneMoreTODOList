package com.artemonre.onemoretodolist.core.ads

import android.app.Activity
import android.content.Context
import androidx.core.content.edit
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

private const val PREFS_NAME = "interstitial_ad"
private const val KEY_LAST_SHOWN_DATE = "last_shown_date"

// Keeps one interstitial preloaded and shows it at most once a day - see
// rememberInterstitialAdTrigger, called after a todo is created or completed. Does NOT preload on
// construction - onAdsAllowed() (called by RequestConsentIfEligible once consent is resolved)
// starts the first load, so nothing is ever requested before that.
class AndroidInterstitialAdController(
    private val context: Context,
    private val adConfig: AdConfig
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private var interstitialAd: InterstitialAd? = null

    fun onAdsAllowed() {
        loadAd()
    }

    fun maybeShow(activity: Activity) {
        val ad = interstitialAd ?: return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        if (lastShownDate() == today) return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                prefs.edit { putString(KEY_LAST_SHOWN_DATE, today.toString()) }
                loadAd()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                interstitialAd = null
                loadAd()
            }
        }
        ad.show(activity)
    }

    private fun lastShownDate(): LocalDate? =
        prefs.getString(KEY_LAST_SHOWN_DATE, null)?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

    private fun loadAd() {
        InterstitialAd.load(
            context,
            adConfig.interstitialAdUnitId,
            nonPersonalizedAdRequest(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }
}
