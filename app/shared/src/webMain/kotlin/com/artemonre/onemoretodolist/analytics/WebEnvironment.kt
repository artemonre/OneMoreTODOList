package com.artemonre.onemoretodolist.analytics

// True on a dev server (jsBrowserDevelopmentRun/wasmJsBrowserDevelopmentRun, or just opening the
// page from a local build) - false for any other host, including one we don't know about yet.
// This only needs to recognize "is this a known dev hostname", not know the real production
// domain, so it works before webApp has an actual deployment to contrast against.
expect fun isLocalhost(): Boolean
