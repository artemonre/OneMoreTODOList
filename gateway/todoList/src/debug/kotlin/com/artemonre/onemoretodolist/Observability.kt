package com.artemonre.onemoretodolist

import android.app.Application
import com.artemonre.onemoretodolist.analytics.Analytics
import com.artemonre.onemoretodolist.analytics.LogcatAnalytics
import org.koin.dsl.module

// Debug builds report nothing - no noise or quota burn from dev-time crashes and test data. The
// release version is in src/release.
@Suppress("UNUSED_PARAMETER")
fun initObservability(application: Application) = Unit

// Every analytics event goes to logcat instead (adb logcat -s Analytics) - replaces analyticsModule's
// PostHogAnalytics, since TodoListApplication lists this module after the shared ones.
val observabilityModule = module {
    single<Analytics> { LogcatAnalytics }
}
