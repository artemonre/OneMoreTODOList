package com.artemonre.onemoretodolist.analytics

import com.posthog.kmp.PostHog
import org.koin.mp.KoinPlatform

// One event as PostHog receives it - built only by AnalyticsEvents, so every event name and
// property key lives in one place (and in ANALYTICS.md). Null property values are left out of the
// event entirely, which is how optional properties (e.g. days_on_top for the top todo only) work.
data class AnalyticsEvent(val name: String, val properties: Map<String, Any?> = emptyMap())

interface Analytics {
    fun capture(event: AnalyticsEvent)
}

// Before initPostHog() has run - debug builds, a checkout without an API key - PostHog.capture
// sends nothing, so this needs no guard of its own.
object PostHogAnalytics : Analytics {
    override fun capture(event: AnalyticsEvent) {
        PostHog.capture(event = event.name, properties = event.properties)
    }
}

// For code outside Koin's reach that also runs without Koin - Compose previews of the forms that
// use speech input or permission prompts. Sends through the app's Analytics while Koin is running,
// and does nothing otherwise.
fun captureIfKoinRunning(event: AnalyticsEvent) {
    KoinPlatform.getKoinOrNull()?.getOrNull<Analytics>()?.capture(event)
}

// For tests and previews, where nothing should be sent.
object NoOpAnalytics : Analytics {
    override fun capture(event: AnalyticsEvent) = Unit
}
