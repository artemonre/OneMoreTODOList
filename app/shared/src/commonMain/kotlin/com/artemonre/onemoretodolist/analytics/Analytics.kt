package com.artemonre.onemoretodolist.analytics

import com.posthog.kmp.PostHog
import com.posthog.kmp.PostHogConfig
import com.posthog.kmp.PostHogContext

// No-ops when apiKey is null/blank, same as initSentry() - a fresh checkout (or CI without
// secrets) runs fine without analytics.
//
// platform is registered as a super property (sent with every event) so events from Android,
// desktop, and web are distinguishable in the PostHog dashboard - there's otherwise nothing in an
// event's shape that says which app/target it came from.
fun initPostHog(apiKey: String?, context: PostHogContext, platform: String) {
    if (apiKey.isNullOrBlank()) return
    PostHog.setup(
        config = PostHogConfig(apiKey = apiKey, host = PostHogConfig.HOST_US),
        context = context,
    )
    PostHog.register("platform", platform)
}
