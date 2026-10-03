package com.artemonre.onemoretodolist.feature.backup.domain

import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem

// "Newest copy wins", per todo id: an incoming copy is taken when there's no local copy, or when
// it's strictly newer (updatedAt) than the local one - a deletion (tombstone) wins the same way as
// an edit. Ties keep the local copy, so re-applying the same data is a no-op. Returns only the
// winners, i.e. exactly what needs writing. Shared by import, Drive restore and sync.
//
// The local copy's topSince is carried over: it's this device's own bookkeeping, which incoming
// copies never include.
fun mergeTodos(local: List<TodoItem>, incoming: List<TodoItem>): List<TodoItem> {
    val localById = local.associateBy { it.id }
    return incoming
        // A file or response listing the same id twice: only its newest copy counts.
        .groupBy { it.id }
        .map { (_, copies) -> copies.maxBy { it.updatedAt } }
        .mapNotNull { candidate ->
            val existing = localById[candidate.id]
            when {
                existing == null -> candidate
                candidate.updatedAt > existing.updatedAt -> candidate.copy(topSince = existing.topSince)
                else -> null
            }
        }
}
