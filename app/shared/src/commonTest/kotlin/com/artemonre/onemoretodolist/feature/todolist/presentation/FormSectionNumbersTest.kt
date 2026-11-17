package com.artemonre.onemoretodolist.feature.todolist.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

// Holo prints the form's section numbers, so they must count only the sections actually shown.
class FormSectionNumbersTest {

    @Test
    fun `the full form numbers tags, checklist, then reminder and repeat`() {
        assertEquals(
            mapOf(FormSection.Tags to 1, FormSection.Checklist to 2, FormSection.ReminderAndRepeat to 3),
            formSectionNumbers(showTags = true, showChecklist = true, showReminderAndRepeat = true)
        )
    }

    @Test
    fun `a hidden section leaves no gap in the numbers`() {
        assertEquals(
            mapOf(FormSection.Checklist to 1, FormSection.ReminderAndRepeat to 2),
            formSectionNumbers(showTags = false, showChecklist = true, showReminderAndRepeat = true)
        )
    }

    @Test
    fun `quick add has no numbered sections`() {
        assertEquals(emptyMap(), formSectionNumbers(showTags = false, showChecklist = false, showReminderAndRepeat = false))
    }
}
