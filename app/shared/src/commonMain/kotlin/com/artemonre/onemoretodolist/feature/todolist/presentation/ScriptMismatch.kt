package com.artemonre.onemoretodolist.feature.todolist.presentation

// Telling whether recognized text was read with the wrong script's model. Each model maps the
// other script's letters onto its own look-alikes, which leaves patterns real text rarely has.
// Only ever used to suggest a switch, never to switch by itself - so it stays on the cautious side.

// Below this many letters there isn't enough text to judge ("OK 15:00" says nothing).
private const val MIN_LETTERS = 20

// Share of words that look garbled - at or above it a Latin read is taken for misread Cyrillic,
// at or below the clean one it's taken for real Latin text.
private const val MISREAD_WORD_SHARE = 0.3f
private const val CLEAN_WORD_SHARE = 0.1f

// Share of letters with no Latin twin - real Russian/Serbian is full of them (и, л, д, я, ж, ...),
// a Latin text pressed into Cyrillic letters has hardly any.
private const val CYRILLIC_NON_TWIN_SHARE = 0.2f

// Cyrillic letters that look the same as a Latin one in print.
private const val CYRILLIC_LATIN_TWINS = "АВЕКМНОРСТХУЅІЈаеорсухѕіј"

// Short units and ordinals glued to a number ("2nd", "10am", "5kg") and short codes ending in one
// ("A4", "mp3") - real Latin words that do mix letters and digits.
private val NUMBER_WITH_SUFFIX = Regex("""\d+\p{L}{1,3}""")
private val CODE_WITH_NUMBER = Regex("""\p{L}{1,3}\d+""")

// Read with the Latin model: is it really Cyrillic? ML Kit turns "задача" into "3agaya" and
// "Третья" into "TpeTbA" - digits standing in for letters and capitals in mid-word.
fun looksLikeMisreadCyrillic(latinReadLines: List<String>): Boolean {
    val share = garbledWordShare(latinReadLines) ?: return false
    return share >= MISREAD_WORD_SHARE
}

// Read with the Cyrillic model: is it really Latin? Judged from a second, Latin-model read of the
// same photo, which comes out clean for Latin text, plus the Cyrillic read having almost no
// letters that only Cyrillic has.
fun looksLikeMisreadLatin(cyrillicReadLines: List<String>, latinReadLines: List<String>): Boolean {
    val share = garbledWordShare(latinReadLines) ?: return false
    if (share > CLEAN_WORD_SHARE) return false
    val letters = cyrillicReadLines.flatMap { line -> line.filter { it.isLetter() }.toList() }
    if (letters.isEmpty()) return true
    val nonTwins = letters.count { it.isCyrillic() && it !in CYRILLIC_LATIN_TWINS }
    return nonTwins.toFloat() / letters.size < CYRILLIC_NON_TWIN_SHARE
}

// The share of words that look like misread Cyrillic, or null when there's too little text.
private fun garbledWordShare(lines: List<String>): Float? {
    val words = lines
        .flatMap { it.split(' ', '\t') }
        .map { word -> word.trim { !it.isLetterOrDigit() } }
        .filter { word -> word.count { it.isLetter() } >= 2 }
    if (words.sumOf { word -> word.count { it.isLetter() } } < MIN_LETTERS) return null
    return words.count { it.looksGarbled() }.toFloat() / words.size
}

private fun String.looksGarbled(): Boolean = hasDigitForLetter() || hasCapitalInside()

private fun String.hasDigitForLetter(): Boolean =
    any { it.isDigit() } && !NUMBER_WITH_SUFFIX.matches(this) && !CODE_WITH_NUMBER.matches(this)

// A capital right after a small letter ("TpeTbA"), or a small letter after two capitals
// ("MOPKOBb") - except the plural "s" of an acronym ("PDFs").
private fun String.hasCapitalInside(): Boolean = indices.drop(1).any { i ->
    val char = this[i]
    when {
        char.isUpperCase() -> this[i - 1].isLowerCase()
        char.isLowerCase() && i >= 2 -> this[i - 1].isUpperCase() && this[i - 2].isUpperCase() &&
            !(char == 's' && i == lastIndex)
        else -> false
    }
}

private fun Char.isCyrillic(): Boolean = this in 'Ѐ'..'ӿ'
