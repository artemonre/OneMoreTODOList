package com.artemonre.onemoretodolist.feature.todolist.domain

// Reliable delivery here needs real Web Push (service worker + a server that stores each
// subscription and fires it at the right time) - not a small addition, this app has no todo sync
// or push infrastructure server-side at all today. Waiting on a possible future cloud-notification
// layer shared with mobile, rather than building a web-only, closed-tab-can't-fire stopgap now.
actual val isDueTimeUiSupported: Boolean = false
