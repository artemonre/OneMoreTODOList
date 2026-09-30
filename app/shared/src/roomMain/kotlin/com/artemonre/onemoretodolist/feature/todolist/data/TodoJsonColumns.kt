package com.artemonre.onemoretodolist.feature.todolist.data

import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Storage shapes for TodoEntity's JSON text columns - kept separate from the domain models so the
// domain stays free of serialization annotations, and so the stored format can't drift just
// because a domain class gets a field renamed.
@Serializable
private data class ChecklistItemDto(val id: String, val text: String, val isDone: Boolean = false)

// color is the TagColor name, not its ordinal, so reordering the enum can't recolor old tags.
@Serializable
private data class TodoTagDto(val name: String, val color: String)

private val json = Json { ignoreUnknownKeys = true }

internal fun List<ChecklistItem>.toChecklistJson(): String =
    json.encodeToString(map { ChecklistItemDto(it.id, it.text, it.isDone) })

// A malformed column (never written by this code, but not worth crashing the whole list over)
// reads back as empty rather than throwing.
internal fun String.toChecklist(): List<ChecklistItem> = try {
    json.decodeFromString<List<ChecklistItemDto>>(this).map { ChecklistItem(it.id, it.text, it.isDone) }
} catch (e: SerializationException) {
    emptyList()
} catch (e: IllegalArgumentException) {
    emptyList()
}

internal fun List<TodoTag>.toTagsJson(): String =
    json.encodeToString(map { TodoTagDto(it.name, it.color.name) })

internal fun String.toTags(): List<TodoTag> = try {
    json.decodeFromString<List<TodoTagDto>>(this).map { dto ->
        TodoTag(dto.name, TagColor.entries.firstOrNull { it.name == dto.color } ?: TagColor.Blue)
    }
} catch (e: SerializationException) {
    emptyList()
} catch (e: IllegalArgumentException) {
    emptyList()
}
