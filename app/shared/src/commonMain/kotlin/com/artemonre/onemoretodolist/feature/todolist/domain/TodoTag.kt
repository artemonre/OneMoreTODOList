package com.artemonre.onemoretodolist.feature.todolist.domain

// Tags live on each todo, not in a separate library - a tag exists exactly as long as some todo
// still carries it. The same name is treated as the same tag: the form suggests tags already in
// use (see knownTags) and reuses their color, so "Work" stays one color across todos.
data class TodoTag(
    val name: String,
    val color: TagColor
)

// Numbered slots, not concrete colors - the design system maps each one to a light/dark-aware
// color pair taken from the app's palettes (see AccentSwatches in core/designsystem/theme).
enum class TagColor {
    Color1, Color2, Color3, Color4, Color5, Color6, Color7, Color8;

    companion object {
        // The color a tag falls back to when its stored color can't be read.
        val Fallback = Color6

        // Before the slots were numbered they had hue names, still found in older saved todos and
        // backups - read by position, so each old tag keeps its slot.
        private val legacyNames = listOf("Red", "Orange", "Yellow", "Green", "Teal", "Blue", "Purple", "Pink")

        /** The slot saved as [name] - current or old-style, any letter case - or null if there's none. */
        fun fromStoredName(name: String): TagColor? =
            entries.firstOrNull { it.name.equals(name, ignoreCase = true) }
                ?: legacyNames.indexOfFirst { it.equals(name, ignoreCase = true) }
                    .takeIf { it >= 0 }
                    ?.let { entries[it] }
    }
}

// Every distinct tag across all todos, first-seen color winning for a name used with more than one
// color (only possible via old data or a race), sorted by name for a stable suggestion order.
fun List<TodoItem>.knownTags(): List<TodoTag> =
    flatMap { it.tags }
        .distinctBy { it.name.lowercase() }
        .sortedBy { it.name.lowercase() }
