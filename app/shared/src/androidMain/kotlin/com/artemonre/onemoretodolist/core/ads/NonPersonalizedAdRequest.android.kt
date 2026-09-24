package com.artemonre.onemoretodolist.core.ads

import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdRequest

// We serve non-personalized ads only, as a deliberate privacy choice - see the app's privacy
// policy. This overrides ad selection for every user regardless of their UMP consent choice, so
// it must be applied to every ad request rather than relying on consent-driven defaults.
fun nonPersonalizedAdRequest(): AdRequest =
    AdRequest.Builder()
        .addNetworkExtrasBundle(AdMobAdapter::class.java, Bundle().apply { putString("npa", "1") })
        .build()
