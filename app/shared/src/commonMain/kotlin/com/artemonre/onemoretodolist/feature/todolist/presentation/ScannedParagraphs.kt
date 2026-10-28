package com.artemonre.onemoretodolist.feature.todolist.presentation

import kotlin.math.abs

// A line was only wrapped onto the next one when it runs (almost) as wide as the block's widest
// line - list items end short and ragged.
private const val WRAPPED_LINE_MIN_WIDTH_SHARE = 0.8f

// ...and is long enough to be running text at all - about 15+ characters, measured in its own line
// heights so it holds at any photo size. Keeps short items ("milk", "bread") apart even when
// they're the widest lines of their block.
private const val WRAPPED_LINE_MIN_WIDTH_IN_HEIGHTS = 8

// Engines sometimes cut one paragraph into separate blocks, so a block whose first line sits right
// under the previous block's last one - at most a line height below, left edges lined up within a
// line height - is read on as the same block. Separate cards, notes and columns sit further apart.
private const val ADJACENT_BLOCK_MAX_GAP_IN_HEIGHTS = 1f
private const val ADJACENT_BLOCK_MAX_LEFT_SHIFT_IN_HEIGHTS = 1f

// A next line starting like this begins an item of its own: a bullet, a dash or a number ("1.", "2)").
private val LIST_ITEM_START = Regex("""^([•·▪◦*\-–—]|\d+[.)])\s""")

// A line ending mid-sentence like this always carries on into the next: "Смахни влево," + "чтобы".
private val UNFINISHED_END = Regex("""[,:]$""")

// A word broken over two lines with a hyphen (plain, Unicode or soft): "распо-" + "знавание".
private val HYPHENATED_END = Regex("""\p{L}[-‐­]$""")

// One recognized line and where it sits, in the photo's own pixels.
data class RecognizedLine(val text: String, val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val height: Int get() = bottom - top
}

// A piece of text that reads as one: a sentence wrapped over several lines, or a single list item.
// Keeps its original lines, so the review can split it back apart when the guess was wrong.
data class ScannedParagraph(val lines: List<String>) {
    val text: String get() = joinWrappedLines(lines)
}

// Each engine block (top to bottom, its lines in reading order) split into paragraphs: a line joins
// the one above only when that one looks wrapped and this one looks like its continuation.
// Anything in doubt stays its own row - the review can merge rows, as well as split joined ones.
fun List<List<RecognizedLine>>.toScannedParagraphs(): List<ScannedParagraph> = joinAdjacentBlocks().flatMap { block ->
    val lines = block.filter { it.text.isNotBlank() }
    if (lines.isEmpty()) return@flatMap emptyList()
    // The widest single line, not the block's whole extent - a short line set off to one side (a
    // date at the right) would otherwise make every text line look short.
    val blockWidth = lines.maxOf { it.right - it.left }
    val paragraphs = mutableListOf(mutableListOf(lines.first()))
    lines.zipWithNext { previous, next ->
        if (continuesWrapped(previous, next, blockWidth)) {
            paragraphs.last() += next
        } else {
            paragraphs += mutableListOf(next)
        }
    }
    paragraphs.map { paragraph -> ScannedParagraph(paragraph.map { it.text.trim() }) }
}

private fun List<List<RecognizedLine>>.joinAdjacentBlocks(): List<List<RecognizedLine>> {
    val joined = mutableListOf<MutableList<RecognizedLine>>()
    map { block -> block.filter { it.text.isNotBlank() } }.filter { it.isNotEmpty() }.forEach { block ->
        val previous = joined.lastOrNull()
        if (previous != null && block.first().sitsRightUnder(previous.last())) {
            previous += block
        } else {
            joined += block.toMutableList()
        }
    }
    return joined
}

private fun RecognizedLine.sitsRightUnder(above: RecognizedLine): Boolean {
    val lineHeight = maxOf(height, above.height)
    // A line without a box (zero height) can't be placed, so it's never taken for a neighbor.
    if (lineHeight <= 0 || top <= above.top) return false
    return top - above.bottom <= lineHeight * ADJACENT_BLOCK_MAX_GAP_IN_HEIGHTS &&
        abs(left - above.left) <= lineHeight * ADJACENT_BLOCK_MAX_LEFT_SHIFT_IN_HEIGHTS
}

private fun continuesWrapped(previous: RecognizedLine, next: RecognizedLine, blockWidth: Int): Boolean {
    val nextText = next.text.trim()
    if (LIST_ITEM_START.containsMatchIn(nextText)) return false
    // Running text carries on in lowercase; a capital more often starts a new item or sentence, and
    // a digit a date or a time of its own. Opening quotes and brackets are looked past.
    val nextStartsLowercase = nextText.firstOrNull { it.isLetterOrDigit() }?.isLowerCase() == true
    if (!nextStartsLowercase) return false
    val previousText = previous.text.trim()
    if (UNFINISHED_END.containsMatchIn(previousText) || HYPHENATED_END.containsMatchIn(previousText)) return true
    val width = previous.right - previous.left
    return width >= blockWidth * WRAPPED_LINE_MIN_WIDTH_SHARE &&
        width >= previous.height * WRAPPED_LINE_MIN_WIDTH_IN_HEIGHTS
}

// Wrapped lines back into one: a word broken with a hyphen is rejoined without it, everything else
// with a single space.
fun joinWrappedLines(lines: List<String>): String = lines
    .map { it.trim() }
    .filter { it.isNotEmpty() }
    .fold("") { joined, line ->
        when {
            joined.isEmpty() -> line
            HYPHENATED_END.containsMatchIn(joined) && line.first().isLowerCase() -> joined.dropLast(1) + line
            else -> "$joined $line"
        }
    }
