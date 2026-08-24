package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlinx.datetime.LocalDate

data class TodoItem(
    val id: String,
    val title: String,
    val isDone: Boolean,
    val sortOrder: Int,
    val date: LocalDate
)
