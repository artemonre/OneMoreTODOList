package com.artemonre.onemoretodolist.feature.todolist.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlinx.datetime.LocalDate

@Entity(tableName = "todo_items")
data class TodoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val isDone: Boolean,
    val sortOrder: Int,
    val date: LocalDate,
    val priorityOrder: Double?
)
