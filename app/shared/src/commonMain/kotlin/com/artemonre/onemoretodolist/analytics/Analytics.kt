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
//
// $geoip_disable=true tells PostHog's ingestion pipeline to skip GeoIP enrichment entirely for
// every event this SDK sends - no country/city/lat-long derived from the IP at all, not just the
// raw IP discarded afterwards. We don't need location data and don't want it in scope for a
// privacy policy. Registered client-side (rather than only relying on the project's dashboard
// settings) so it's enforced from code regardless of how the PostHog project is configured.
fun initPostHog(apiKey: String?, context: PostHogContext, platform: String) {
    if (apiKey.isNullOrBlank()) return
    PostHog.setup(
        config = PostHogConfig(apiKey = apiKey, host = PostHogConfig.HOST_US),
        context = context,
    )
    PostHog.register("platform", platform)
    PostHog.register("\$geoip_disable", true)
}
