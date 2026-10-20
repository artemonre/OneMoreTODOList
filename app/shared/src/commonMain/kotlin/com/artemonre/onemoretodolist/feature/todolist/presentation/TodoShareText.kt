package com.artemonre.onemoretodolist.feature.todolist.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag

/**
 * The todo as plain text for sharing and copying - text, checklist, reminder, then tags as
 * hashtags. Plain characters rather than Markdown, since WhatsApp, Telegram, SMS and Gmail don't
 * reliably render it. Priority and recurrence are left out on purpose: they're the sender's own
 * organisation and mean nothing to whoever receives it. [reminder] comes preformatted (it needs
 * localized month names, see [rememberShareText]).
 */
fun TodoItemUi.toShareText(reminder: String?): String = buildString {
    append(text)
    checklist.forEach { entry ->
        append('\n').append(if (entry.isDone) "☑ " else "☐ ").append(entry.text)
    }
    reminder?.let { append("\n⏰ ").append(it) }
    val hashtags = tags.mapNotNull { it.toHashtag() }
    if (hashtags.isNotEmpty()) append('\n').append(hashtags.joinToString(" "))
}

// A hashtag ends at the first non-word character, so "#work stuff" would only link "#work" -
// every run of anything that isn't a letter or digit becomes a single underscore instead. Null
// for a name with nothing usable left.
internal fun TodoTag.toHashtag(): String? =
    name.map { if (it.isLetterOrDigit()) it else ' ' }
        .joinToString("")
        .split(' ')
        .filter { it.isNotEmpty() }
        .joinToString("_")
        .takeIf { it.isNotEmpty() }
        ?.let { "#$it" }

@Composable
fun rememberShareText(item: TodoItemUi): String {
    val dateFormat = rememberShortDateFormat()
    return remember(item, dateFormat) {
        val reminder = item.dueDate?.let { date ->
            listOfNotNull(dateFormat.format(date), item.dueTime?.let(dueTimeFormat::format)).joinToString(", ")
        }
        item.toShareText(reminder)
    }
}
