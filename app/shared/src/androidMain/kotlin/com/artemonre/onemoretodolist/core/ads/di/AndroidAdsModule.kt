package com.artemonre.onemoretodolist.core.ads.di

import android.content.Context
import com.artemonre.onemoretodolist.core.ads.AdConfig
import com.artemonre.onemoretodolist.core.ads.AdConsentState
import com.artemonre.onemoretodolist.core.ads.AndroidConsentEligibilityTracker
import com.artemonre.onemoretodolist.core.ads.AndroidInterstitialAdController
import com.artemonre.onemoretodolist.core.ads.ConsentEligibilityTracker
import com.artemonre.onemoretodolist.core.ads.DebugConsentSettingsProvider
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module

// Overrides the common adsModule's default (test ad unit) AdConfig and NoOp consent tracker with a
// product's real AdMob IDs and the real SharedPreferences-backed tracker, and registers the
// Android-only interstitial controller + consent gate - same override pattern androidDueTimeModule
// uses for DueTimeScheduler.
fun androidAdsModule(context: Context, adConfig: AdConfig): Module = module {
    single { adConfig }
    single { AndroidInterstitialAdController(context, get()) }
    single { AdConsentState() }
    // bind: RequestConsentIfEligible needs the concrete type for hasPerformedTodoAction() (not
    // part of the common interface), while TodoListViewModel injects the ConsentEligibilityTracker
    // interface - both resolve to this same singleton instance.
    single { AndroidConsentEligibilityTracker(context) } bind ConsentEligibilityTracker::class
    // Default no-op - overridden by gateway/todoList's debug-only module (see debugAdsModule),
    // never by its release one, same override pattern as everything else here.
    single<DebugConsentSettingsProvider> { DebugConsentSettingsProvider { null } }
}
