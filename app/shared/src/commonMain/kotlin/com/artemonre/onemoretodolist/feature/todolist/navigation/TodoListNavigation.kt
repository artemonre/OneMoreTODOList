package com.artemonre.onemoretodolist.feature.todolist.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.artemonre.onemoretodolist.feature.todolist.presentation.TodoListRoot

fun EntryProviderScope<NavKey>.todoListEntries() {
    entry(TodoListRoute.List) {
        TodoListRoot()
    }
}
