package com.artemonre.onemoretodolist.feature.todolist.domain

// Tags live on each todo, not in a separate library - a tag exists exactly as long as some todo
// still carries it. The same name is treated as the same tag: the form suggests tags already in
// use (see knownTags) and reuses their color, so "Work" stays one color across todos.
data class TodoTag(
    val name: String,
    val color: TagColor
)

// Semantic slots, not concrete colors - the design system maps each one to a light/dark-aware
// color pair (see TagColors in core/designsystem/theme).
enum class TagColor { Red, Orange, Yellow, Green, Teal, Blue, Purple, Pink }

// Every distinct tag across all todos, first-seen color winning for a name used with more than one
// color (only possible via old data or a race), sorted by name for a stable suggestion order.
fun List<TodoItem>.knownTags(): List<TodoTag> =
    flatMap { it.tags }
        .distinctBy { it.name.lowercase() }
        .sortedBy { it.name.lowercase() }
