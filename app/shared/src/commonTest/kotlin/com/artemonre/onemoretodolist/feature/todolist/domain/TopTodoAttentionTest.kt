package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class TopTodoAttentionTest {

    private val now = Clock.System.now()

    @Test
    fun `not currently top is None`() {
        assertEquals(TopTodoAttention.None, topTodoAttention(topSince = null, now = now))
    }

    @Test
    fun `still within the grace period is None`() {
        assertEquals(TopTodoAttention.None, topTodoAttention(topSince = now, now = now))
        assertEquals(TopTodoAttention.None, topTodoAttention(topSince = now - 1.days, now = now))
        assertEquals(TopTodoAttention.None, topTodoAttention(topSince = now - 2.days + 1.hours, now = now))
    }

    @Test
    fun `two days becomes Primary`() {
        assertEquals(TopTodoAttention.Primary, topTodoAttention(topSince = now - 2.days, now = now))
        assertEquals(TopTodoAttention.Primary, topTodoAttention(topSince = now - 3.days + 1.hours, now = now))
    }

    @Test
    fun `three days becomes PrimaryContainer`() {
        assertEquals(TopTodoAttention.PrimaryContainer, topTodoAttention(topSince = now - 3.days, now = now))
        assertEquals(TopTodoAttention.PrimaryContainer, topTodoAttention(topSince = now - 4.days + 1.hours, now = now))
    }

    @Test
    fun `four or more days becomes and stays Error`() {
        assertEquals(TopTodoAttention.Error, topTodoAttention(topSince = now - 4.days, now = now))
        assertEquals(TopTodoAttention.Error, topTodoAttention(topSince = now - 30.days, now = now))
    }
}
