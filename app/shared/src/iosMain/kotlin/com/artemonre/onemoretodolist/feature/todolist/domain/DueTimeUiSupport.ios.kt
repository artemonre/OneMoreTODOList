package com.artemonre.onemoretodolist.feature.todolist.domain

// UNUserNotificationCenter is the real iOS equivalent of AlarmManager and this is buildable in
// principle (see ExactAlarmPermission.ios.kt/DueTimeNotificationPermission.ios.kt for the same
// "null on iOS for now" shape elsewhere) - blocked purely on tooling, not feasibility: Kotlin/
// Native's iOS backend only compiles on macOS, and this project is developed on Windows. Flip this
// to true once the real scheduler is written and verified from a Mac.
actual val isDueTimeUiSupported: Boolean = false
