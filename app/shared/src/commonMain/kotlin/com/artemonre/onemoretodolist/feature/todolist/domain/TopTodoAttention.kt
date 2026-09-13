package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Instant

enum class TopTodoAttention { None, Primary, PrimaryContainer, Error }

// Escalates the longer a todo has continuously been the top of the active list (topSince is kept
// in sync by TopSinceTrackingTodoLocalDataSource) - the day it becomes top and the next day are a
// grace period (None), then it ramps up one level per day until it maxes out at Error and stays
// there until another todo takes its place.
fun topTodoAttention(topSince: Instant?, now: Instant = Clock.System.now()): TopTodoAttention {
    if (topSince == null) return TopTodoAttention.None
    val age = now - topSince
    return when {
        age < 2.days -> TopTodoAttention.None
        age < 3.days -> TopTodoAttention.Primary
        age < 4.days -> TopTodoAttention.PrimaryContainer
        else -> TopTodoAttention.Error
    }
}
