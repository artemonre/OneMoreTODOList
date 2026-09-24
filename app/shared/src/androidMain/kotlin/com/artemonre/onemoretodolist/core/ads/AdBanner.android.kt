package com.artemonre.onemoretodolist.core.ads

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import org.koin.compose.koinInject

@Composable
actual fun AdBanner(modifier: Modifier) {
    // Nothing is requested at all until consent is resolved (not required, or given) - see
    // AdConsentState/RequestConsentIfEligible.
    val adConsentState = koinInject<AdConsentState>()
    val adsAllowed by adConsentState.adsAllowed.collectAsStateWithLifecycle()
    if (!adsAllowed) return

    val context = LocalContext.current
    val adConfig = koinInject<AdConfig>()
    // Adaptive banner: spans the device's actual width (Google computes the height itself, a bit
    // taller than the old fixed 320x50 on larger screens) instead of a fixed size sitting
    // left-aligned with empty space next to it. Recomputed whenever the available width changes
    // (e.g. a rotation) - Google's own guidance for adaptive banners is to reload with a freshly
    // computed size rather than leave the old orientation's creative in place.
    val adWidthDp = LocalConfiguration.current.screenWidthDp
    val adSize = remember(adWidthDp) {
        AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidthDp)
    }

    AndroidView(
        modifier = modifier.fillMaxWidth().wrapContentHeight(),
        factory = {
            AdView(context).apply {
                adUnitId = adConfig.bannerAdUnitId
            }
        },
        update = { adView ->
            // There's no way to resize an already-loaded ad's creative in place - a fresh
            // AdRequest is required, so this only reloads when the size actually changed instead
            // of on every unrelated recomposition. adView.adSize starts out null, so this also
            // covers the very first load, unifying it with the rotation-reload path.
            if (adView.adSize != adSize) {
                adView.setAdSize(adSize)
                adView.loadAd(nonPersonalizedAdRequest())
            }
        },
        onRelease = { it.destroy() }
    )
}
