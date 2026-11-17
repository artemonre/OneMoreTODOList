package com.artemonre.onemoretodolist.feature.todolist.domain

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Tag colors are saved by name - in the database, in backups and by agents - so every name ever
// written must keep reading as the same slot.
class TagColorTest {

    @Test
    fun `current names read back as their slot, in any letter case`() {
        TagColor.entries.forEach { color ->
            assertEquals(color, TagColor.fromStoredName(color.name))
            assertEquals(color, TagColor.fromStoredName(color.name.uppercase()))
        }
    }

    @Test
    fun `old hue names read as the slot in the same position`() {
        val oldNames = listOf("Red", "Orange", "Yellow", "Green", "Teal", "Blue", "Purple", "Pink")
        oldNames.forEachIndexed { index, name ->
            assertEquals(TagColor.entries[index], TagColor.fromStoredName(name))
        }
        assertEquals(TagColor.Color6, TagColor.fromStoredName("BLUE"))
    }

    @Test
    fun `an unknown name reads as nothing`() {
        assertNull(TagColor.fromStoredName("Beige"))
        assertNull(TagColor.fromStoredName(""))
    }
}
