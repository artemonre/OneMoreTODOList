package com.artemonre.onemoretodolist.core.domain

// Work a feature wants done every time the app comes to the foreground (seeding, catch-ups,
// cleanup). Features register theirs in Koin; the container runs them all without knowing what
// they are, so core never depends on a feature.
fun interface AppStartTask {
    suspend fun run()
}
