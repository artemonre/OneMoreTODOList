package com.artemonre.onemoretodolist.core.ads

// Google's official public test ad unit IDs (safe to ship as-is, always serve test creatives) -
// the default Koin binding in todoListModule. A real product build overrides this binding with
// its own real AdMob IDs - see gateway/todoList's admob.properties and TodoListApplication.
data class AdConfig(
    val bannerAdUnitId: String = "ca-app-pub-3940256099942544/9214589741",
    val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712"
)
