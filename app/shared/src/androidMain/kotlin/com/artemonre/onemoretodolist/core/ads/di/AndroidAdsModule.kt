package com.artemonre.onemoretodolist.core.ads.di

import android.content.Context
import com.artemonre.onemoretodolist.core.ads.AdConfig
import com.artemonre.onemoretodolist.core.ads.AndroidInterstitialAdController
import org.koin.core.module.Module
import org.koin.dsl.module

// Overrides the common adsModule's default (test ad unit) AdConfig with a product's real AdMob
// IDs, and registers the Android-only interstitial controller - same override pattern
// androidDueTimeModule uses for DueTimeScheduler.
fun androidAdsModule(context: Context, adConfig: AdConfig): Module = module {
    single { adConfig }
    single { AndroidInterstitialAdController(context, get()) }
}
