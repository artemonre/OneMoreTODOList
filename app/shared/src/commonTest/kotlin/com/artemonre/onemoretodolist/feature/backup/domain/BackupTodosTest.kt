package com.artemonre.onemoretodolist.feature.backup.domain

import com.artemonre.onemoretodolist.core.domain.Result
import com.artemonre.onemoretodolist.feature.backup.data.JsonTodoBackupCodec
import com.artemonre.onemoretodolist.feature.todolist.domain.ChecklistItem
import com.artemonre.onemoretodolist.feature.todolist.domain.FakeTodoLocalDataSource
import com.artemonre.onemoretodolist.feature.todolist.domain.NoOpDueTimeScheduler
import com.artemonre.onemoretodolist.feature.todolist.domain.Recurrence
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceType
import com.artemonre.onemoretodolist.feature.todolist.domain.RecurrenceUnit
import com.artemonre.onemoretodolist.feature.todolist.domain.TagColor
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoItem
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoStatus
import com.artemonre.onemoretodolist.feature.todolist.domain.TodoTag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

class BackupTodosTest {

    private val older = Instant.fromEpochMilliseconds(1_000)
    private val newer = Instant.fromEpochMilliseconds(2_000)
    private val now = Instant.fromEpochMilliseconds(9_000)
    private val codec = JsonTodoBackupCodec()
    private val fixedClock = object : Clock {
        override fun now(): Instant = this@BackupTodosTest.now
    }

    @Test
    fun `merge takes newer and new copies and ignores older ones`() {
        val local = listOf(todo("a", "Local A", newer), todo("b", "Local B", older))
        val incoming = listOf(todo("a", "Incoming A", older), todo("b", "Incoming B", newer), todo("c", "Incoming C", older))

        val winners = mergeTodos(local, incoming)

        assertEquals(listOf("Incoming B", "Incoming C"), winners.map { it.text })
    }

    @Test
    fun `merge lets a newer tombstone delete and keeps the local topSince`() {
        val topSince = Instant.fromEpochMilliseconds(500)
        val local = listOf(todo("a", "A", older).copy(topSince = topSince))
        val incoming = listOf(todo("a", "A", newer).copy(deletedAt = newer))

        val winner = mergeTodos(local, incoming).single()

        assertEquals(newer, winner.deletedAt)
        assertEquals(topSince, winner.topSince)
    }

    @Test
    fun `export then merge-import into an empty list restores every field`() = runTest {
        val original = todo("a", "Full", older).copy(
            priorityOrder = 0.9,
            recurrence = Recurrence(RecurrenceType.Every, 2, RecurrenceUnit.Week),
            dueDate = LocalDate(2026, 3, 4),
            dueTime = LocalTime(9, 30),
            checklist = listOf(ChecklistItem(id = "c1", text = "Step", isDone = true)),
            tags = listOf(TodoTag("Work", TagColor.Teal))
        )
        val json = ExportTodos(FakeTodoLocalDataSource(listOf(original)), codec, fixedClock)()
        val target = FakeTodoLocalDataSource()

        val result = ImportTodos(target, codec, NoOpDueTimeScheduler(), fixedClock)(json, ImportMode.Merge)

        assertEquals(Result.Success(ImportSummary(importedCount = 1)), result)
        assertEquals(listOf(original), target.observeTodos().first())
    }

    @Test
    fun `export leaves deleted todos out`() = runTest {
        val source = FakeTodoLocalDataSource(listOf(todo("a", "Kept", older), todo("b", "Gone", older).copy(deletedAt = older)))
        val target = FakeTodoLocalDataSource()

        ImportTodos(target, codec, NoOpDueTimeScheduler(), fixedClock)(ExportTodos(source, codec, fixedClock)(), ImportMode.Merge)

        assertEquals(listOf("Kept"), target.getAllIncludingDeleted().map { it.text })
    }

    @Test
    fun `replace deletes todos missing from the file and stamps the file's todos as new`() = runTest {
        val json = ExportTodos(FakeTodoLocalDataSource(listOf(todo("a", "From file", older))), codec, fixedClock)()
        val target = FakeTodoLocalDataSource(listOf(todo("a", "Local A", newer), todo("b", "Only local", newer)))

        ImportTodos(target, codec, NoOpDueTimeScheduler(), fixedClock)(json, ImportMode.Replace)

        val stored = target.getAllIncludingDeleted().associateBy { it.id }
        assertEquals("From file", stored.getValue("a").text)
        assertEquals(now, stored.getValue("a").updatedAt)
        assertNull(stored.getValue("a").deletedAt)
        assertNotNull(stored.getValue("b").deletedAt)
        assertEquals(listOf("a"), target.observeTodos().first().map { it.id })
    }

    @Test
    fun `garbage and newer-format files are rejected without touching the list`() = runTest {
        val target = FakeTodoLocalDataSource(listOf(todo("a", "A", older)))
        val importTodos = ImportTodos(target, codec, NoOpDueTimeScheduler(), fixedClock)

        assertEquals(Result.Error(BackupError.INVALID_FILE), importTodos("not json", ImportMode.Replace))
        assertEquals(Result.Error(BackupError.INVALID_FILE), importTodos("""{"hello": 1}""", ImportMode.Replace))
        assertEquals(
            Result.Error(BackupError.NEWER_FORMAT),
            importTodos("""{"formatVersion": 99, "exportedAt": 0, "todos": []}""", ImportMode.Replace)
        )
        assertEquals(listOf("A"), target.observeTodos().first().map { it.text })
    }

    @Test
    fun `a todo with an unreadable field is skipped, the rest still import`() = runTest {
        val json = """
            {"formatVersion": 1, "exportedAt": 0, "todos": [
              {"id": "ok", "text": "Fine", "status": "Active", "sortOrder": 0,
               "creationDate": "2026-01-01", "lastEditDate": "2026-01-01", "updatedAt": 1000},
              {"id": "bad", "text": "Broken", "status": "Someday", "sortOrder": 1,
               "creationDate": "2026-01-01", "lastEditDate": "2026-01-01", "updatedAt": 1000}
            ]}
        """.trimIndent()
        val target = FakeTodoLocalDataSource()

        ImportTodos(target, codec, NoOpDueTimeScheduler(), fixedClock)(json, ImportMode.Merge)

        assertEquals(listOf("ok"), target.observeTodos().first().map { it.id })
    }

    private fun todo(id: String, text: String, updatedAt: Instant) = TodoItem(
        id = id,
        text = text,
        status = TodoStatus.Active,
        sortOrder = 0,
        creationDate = LocalDate(2026, 1, 1),
        lastEditDate = LocalDate(2026, 1, 1),
        updatedAt = updatedAt
    )
}
