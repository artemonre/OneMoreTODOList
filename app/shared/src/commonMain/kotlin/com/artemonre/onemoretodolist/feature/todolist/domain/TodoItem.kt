package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.datetime.LocalDate

data class TodoItem(
    val id: String,
    val title: String,
    val status: TodoStatus,
    val sortOrder: Int,
    val date: LocalDate,
    // Rank among other prioritized items; null means not prioritized, lower sorts first.
    // New values are assigned as (currentMin ?: 1.0) * 0.9, always positive, never needing
    // to renumber existing items. Only affects TodoSortOption.Date.
    val priorityOrder: Double? = null
)
