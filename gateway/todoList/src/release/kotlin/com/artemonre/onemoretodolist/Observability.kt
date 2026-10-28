package com.artemonre.onemoretodolist

import android.app.Application
import com.artemonre.onemoretodolist.analytics.initPostHog
import com.artemonre.onemoretodolist.observability.initSentry
import com.posthog.kmp.PostHogContext
import org.koin.dsl.module

// Crash reporting and analytics. Also compiled into the Baseline Profile plugin's build types
// (nonMinifiedRelease, benchmarkRelease - see build.gradle.kts), so the profile covers this
// startup code too. Only the release build has the keys, though (see gateway.application's
// buildConfigFields) - everywhere else both calls find an empty one and stay off.
fun initObservability(application: Application) {
    initSentry(BuildConfig.SENTRY_DSN, environment = "production")
    initPostHog(BuildConfig.POSTHOG_API_KEY, PostHogContext(application), platform = "android")
}

// Nothing to replace here - analyticsModule's PostHogAnalytics is the release one. The debug version
// swaps in logcat.
val observabilityModule = module { }
