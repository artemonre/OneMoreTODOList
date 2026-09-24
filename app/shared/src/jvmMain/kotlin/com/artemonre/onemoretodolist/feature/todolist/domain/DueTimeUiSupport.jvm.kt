package com.artemonre.onemoretodolist.feature.todolist.domain

// Still NoOpDueTimeScheduler today (nothing actually fires yet) - kept visible anyway since it's
// concretely planned, not indefinitely blocked like iOS/web: system tray support (minimize instead
// of exiting) plus launch-at-login, then a background coroutine checking due times while the
// process is alive and firing a native tray notification. No OS-level wake-up like AlarmManager,
// so this only ever works while the app is running (tray-minimized counts, fully quit doesn't).
actual val isDueTimeUiSupported: Boolean = true
