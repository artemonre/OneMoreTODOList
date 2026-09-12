package com.artemonre.onemoretodolist.observability

import io.sentry.kotlin.multiplatform.Sentry

// No-ops when dsn is null/blank so a fresh checkout (or CI without secrets) runs fine without
// crash reporting - mirrors how a missing keystore.properties degrades to an unsigned build.
fun initSentry(dsn: String?, environment: String) {
    if (dsn.isNullOrBlank()) return
    Sentry.init { options ->
        options.dsn = dsn
        options.environment = environment
    }
}
