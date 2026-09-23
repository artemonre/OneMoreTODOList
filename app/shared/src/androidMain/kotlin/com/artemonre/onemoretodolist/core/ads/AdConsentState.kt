package com.artemonre.onemoretodolist.core.ads

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Whether AdBanner/AndroidInterstitialAdController are allowed to request any ad at all this
// session - starts false and flips true once RequestConsentIfEligible determines consent isn't
// required, is already obtained, or has just been given. Never flips back to false mid-session;
// a fresh Koin singleton per process start is what makes it re-check on the next app launch.
class AdConsentState {
    private val _adsAllowed = MutableStateFlow(false)
    val adsAllowed: StateFlow<Boolean> = _adsAllowed.asStateFlow()

    fun allowAds() {
        _adsAllowed.value = true
    }
}
