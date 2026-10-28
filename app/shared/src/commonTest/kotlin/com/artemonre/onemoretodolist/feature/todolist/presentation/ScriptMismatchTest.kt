package com.artemonre.onemoretodolist.feature.todolist.presentation

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ScriptMismatchTest {

    // A real ML Kit (Latin model) read of a Russian screenshot of the onboarding todos.
    private val cyrillicReadAsLatin = listOf(
        "TpeTbA 3agaya. Ecnu Haxas «Fó",
        "HaBepx», TO 3aga co3ndcTCA B",
        "03 ceHT. 2026)",
        "TlepBaa 3aga4a",
        "01 ceHT. 2026",
        "BTopaa 3ana4a. llo yMON4aHM",
        "OTCopTMpoBaHbl no qaTe, oT C",
        "02 ceHT. 2026",
        "YeTBёpTaA 3apaga. CMaxHM BI",
        "4TOObll M3MeHMTb Mlu yganM",
        "04 ceHT. 2026",
        "lATag 3aga4a. CMaxHI BlipaBO,"
    )

    private val englishTodos = listOf(
        "Buy milk and bread on the way home",
        "Call the dentist before 10am about the 2nd appointment",
        "Print the A4 report and send the PDFs to the team",
        "Update my iPhone and back up photos"
    )

    private val russianTodos = listOf(
        "Купить молоко и хлеб по дороге домой",
        "Позвонить стоматологу до десяти утра",
        "Распечатать отчёт и отправить его команде"
    )

    @Test
    fun `cyrillic read with the latin model is caught`() {
        assertTrue(looksLikeMisreadCyrillic(cyrillicReadAsLatin))
    }

    @Test
    fun `real english text isn't taken for misread cyrillic`() {
        assertFalse(looksLikeMisreadCyrillic(englishTodos))
    }

    @Test
    fun `too little text is never judged`() {
        assertFalse(looksLikeMisreadCyrillic(listOf("TpeTbA 3agaya")))
        assertFalse(looksLikeMisreadLatin(cyrillicReadLines = listOf("Нецо"), latinReadLines = listOf("Hello")))
    }

    @Test
    fun `latin read with the cyrillic model is caught`() {
        val cyrillicRead = listOf("Вuу мНК аnd ВгеаО оп Не wау Ноте", "СаН Не Оеп1Ы ВеТоге 10ат")
        assertTrue(looksLikeMisreadLatin(cyrillicReadLines = cyrillicRead, latinReadLines = englishTodos))
    }

    @Test
    fun `real russian text isn't taken for misread latin`() {
        assertFalse(looksLikeMisreadLatin(cyrillicReadLines = russianTodos, latinReadLines = cyrillicReadAsLatin))
    }

    @Test
    fun `russian text whose latin read happens to look clean still isn't taken for latin`() {
        // All-lowercase look-alikes can slip past the Latin-side check - the Cyrillic read decides.
        val cleanLookingLatinRead = listOf("kynntb mono ko n xneb no gopore gomon", "pacnevatatb otyet n otnpabntb ero")
        assertFalse(looksLikeMisreadLatin(cyrillicReadLines = russianTodos, latinReadLines = cleanLookingLatinRead))
    }
}
