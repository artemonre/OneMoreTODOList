package com.artemonre.onemoretodolist.feature.todolist.presentation

import kotlin.test.Test
import kotlin.test.assertEquals

class ScannedParagraphsTest {

    private fun line(text: String, right: Int, left: Int = 0, top: Int = 0, height: Int = 40) =
        RecognizedLine(text, left, top, right, top + height)

    @Test
    fun `a sentence wrapped over full-width lines becomes one paragraph`() {
        val block = listOf(
            line("Третья задача. Если нажать «Добавить", right = 900),
            line("наверх», то задача создастся в начале", right = 880),
            line("списка", right = 160)
        )

        val paragraphs = listOf(block).toScannedParagraphs()

        assertEquals(
            listOf("Третья задача. Если нажать «Добавить наверх», то задача создастся в начале списка"),
            paragraphs.map { it.text }
        )
        assertEquals(3, paragraphs.single().lines.size)
    }

    @Test
    fun `short list items stay apart even in lowercase`() {
        val block = listOf(line("milk", right = 150), line("bread", right = 180), line("eggs", right = 150))

        assertEquals(listOf("milk", "bread", "eggs"), listOf(block).toScannedParagraphs().map { it.text })
    }

    @Test
    fun `a full-width line followed by a capital or a list marker isn't joined`() {
        val block = listOf(
            line("Call the dentist about the appointment", right = 900),
            line("Buy milk and bread on the way home", right = 880),
            line("- pick up the parcel at the post office", right = 900),
            line("2. renew the library card before Friday", right = 900)
        )

        assertEquals(4, listOf(block).toScannedParagraphs().size)
    }

    @Test
    fun `a block far below the previous one never joins it`() {
        val first = listOf(line("Вторая задача. По умолчанию задачи", right = 900, top = 0))
        val second = listOf(line("отсортированы по дате", right = 500, top = 200))

        assertEquals(2, listOf(first, second).toScannedParagraphs().size)
    }

    @Test
    fun `a block right under the previous one reads on as the same block`() {
        // Tesseract cut one card's lines into separate blocks.
        val blocks = listOf(
            listOf(line("Вторая задача. По умолчанию задачи", right = 850, top = 0)),
            listOf(line("отсортированы по дате, от старых к новым", right = 900, top = 56)),
            listOf(line("Четвёртая задача. Смахни влево,", right = 600, top = 300)),
            listOf(line("чтобы изменить или удалить", right = 500, top = 356))
        )

        assertEquals(
            listOf(
                "Вторая задача. По умолчанию задачи отсортированы по дате, от старых к новым",
                "Четвёртая задача. Смахни влево, чтобы изменить или удалить"
            ),
            blocks.toScannedParagraphs().map { it.text }
        )
    }

    @Test
    fun `a block beside the previous one doesn't join it`() {
        // Two columns: the second starts at the same height, far to the right.
        val left = listOf(line("Купить молоко,", right = 300, top = 0))
        val right = listOf(line("позвонить маме", left = 500, right = 800, top = 0))

        assertEquals(2, listOf(left, right).toScannedParagraphs().size)
    }

    @Test
    fun `a hyphenated word is rejoined without the hyphen`() {
        val block = listOf(line("Проверить распо-", right = 400), line("знавание текста", right = 380))

        assertEquals(listOf("Проверить распознавание текста"), listOf(block).toScannedParagraphs().map { it.text })
    }

    @Test
    fun `a line ending with a comma or colon carries on whatever its width`() {
        val block = listOf(
            line("Смахни влево,", right = 300),
            line("чтобы изменить или удалить", right = 600),
            line("Купить:", right = 160),
            line("молоко и хлеб", right = 300)
        )

        assertEquals(
            listOf("Смахни влево, чтобы изменить или удалить", "Купить: молоко и хлеб"),
            listOf(block).toScannedParagraphs().map { it.text }
        )
    }

    @Test
    fun `a line starting with a digit isn't joined`() {
        val block = listOf(
            line("отсортированы по дате, от старых к новым", right = 900),
            line("02 сент. 2026", left = 600, right = 900)
        )

        assertEquals(2, listOf(block).toScannedParagraphs().size)
    }

    @Test
    fun `a short line off to the side doesn't make the text lines look short`() {
        // A card: wrapped text on the left, its date right-aligned below.
        val block = listOf(
            line("Пятая задача. Смахни вправо, чтобы", left = 100, right = 640),
            line("поделиться", left = 100, right = 300),
            line("05 сент. 2026", left = 700, right = 1000)
        )

        assertEquals(
            listOf("Пятая задача. Смахни вправо, чтобы поделиться", "05 сент. 2026"),
            listOf(block).toScannedParagraphs().map { it.text }
        )
    }

    @Test
    fun `blank lines are dropped`() {
        val block = listOf(line("  ", right = 100), line("Milk", right = 150))

        assertEquals(listOf(ScannedParagraph(listOf("Milk"))), listOf(block).toScannedParagraphs())
    }
}
