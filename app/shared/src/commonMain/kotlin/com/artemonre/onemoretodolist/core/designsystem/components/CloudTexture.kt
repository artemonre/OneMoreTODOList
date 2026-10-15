package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.hypot
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

// How many consecutive cards one cloud runs through - see [cloudTexture].
const val CLOUD_SEGMENT_COUNT = 5

// A touch of see-through on the whole line, so even its opaque edge sits softly on the card.
private const val LINE_ALPHA = 0.9f
private val STROKE_WIDTH = 3.dp

// Compose has no "shade a Path across its own local width" primitive - a Brush is positioned in
// canvas space, not relative to the path, so it can't follow a curve. Instead this draws the same
// line [RAIL_COUNT] times, each copy shifted a little further along the line's own local normal,
// each a solid color interpolated from opaque to transparent. Overlapping copies read as one smooth
// gradient that hugs the line's shape everywhere.
private const val RAIL_COUNT = 6

// Where the cloud enters and leaves (end side, past the card's edge so it starts off-screen), as a
// fraction of the card's width. Its deepest point - the start-side edge of the cloud - is random
// per cloud, 15%-35% of the width.
private const val CLOUD_OUTER_X = 1.15f
private const val CLOUD_DEEPEST_X_MIN = 0.15f
private const val CLOUD_DEEPEST_X_RANGE = 0.2f

// How far into the first card the cloud enters, and how early in the last card it leaves, as a
// fraction of the whole cloud (one card is 1 / CLOUD_SEGMENT_COUNT = 0.2): random per cloud, from
// right at the card edge to 60% of the way into that card.
private const val CLOUD_EDGE_INSET_MAX = 0.12f

// The outline's roundness: 0.5 would be an exact half-ellipse (a round belly that enters and leaves
// almost sideways), 1.0 a flatter parabola. Just above 0.5 keeps it round while still letting the
// first and last cards carry a good part of the sweep.
private const val CLOUD_ROUNDNESS_EXPONENT = 0.6f

// Average bump length along the line, random per cloud; each bump then varies around it by up to
// BUMP_LENGTH_VARIATION either way.
private const val BUMP_STEP_DP_MIN = 30f
private const val BUMP_STEP_DP_RANGE = 10f
private const val BUMP_LENGTH_VARIATION = 0.35f

// Bump radius as chord / factor, random per bump: 2 is an exact half-circle, smaller is shallower.
private const val BUMP_RADIUS_FACTOR_MIN = 1.5f
private const val BUMP_RADIUS_FACTOR_RANGE = 0.4f

// How finely the cloud's centerline is sampled to measure its length before placing the bumps.
private val CURVE_SAMPLE_STEP = 2.dp

// Mixed into every cloud's seed so the whole set reshuffles on each fresh app process - initialized
// once per process (top-level property), so it's the same for every card drawn during this launch.
private val sessionSeed = Random.nextInt()

// Which part of which cloud a card draws: [cloudIndex] picks the cloud (every card in it shares
// the same random entrance, exit, depth and average bump size), [segment] (0 until CLOUD_SEGMENT_COUNT) the card's slice.
data class CloudSegment(val cloudIndex: Int, val segment: Int)

// One "child's cloud doodle" line - a row of round, overlapping bumps - running through
// CLOUD_SEGMENT_COUNT consecutive cards: it enters off-screen at the end side somewhere in the upper
// part of the first card, bellies out toward the start side, deepest around the middle card, and
// leaves off-screen at the end side somewhere in the lower part of the last card. Each card draws only its own slice of the shared
// curve, mapped onto the card's full bounds - [bleed] is how far the card's edges lie outside this
// modifier's bounds (the card's content padding) - so neighbouring cards meet at exactly the same
// point, and the gap between them reads as the line passing behind it. Each card fits a whole
// number of bumps between its top and bottom edge, so a bump never breaks off mid-way at a card
// edge and the next card's bumps pick up where these end. Bumps vary randomly in length and
// roundness, and always bulge toward the start side.
// The line is shaded across its own width: exactly [color] (at [LINE_ALPHA]) on its outer edge,
// fading to fully transparent - so it ends in the card's own color - on the inner one.
//
// Rendered once into a bitmap per card size/segment (drawWithCache), so scrolling only blits an
// image instead of re-rasterizing the stroked arcs every frame. Relies on the caller's clip (a
// Card's shape) to crop it.
fun Modifier.cloudTexture(cloudSegment: CloudSegment, color: Color, bleed: Dp = 0.dp): Modifier = drawWithCache {
    val bleedPx = bleed.toPx()
    val cardWidth = size.width + 2 * bleedPx
    val cardHeight = size.height + 2 * bleedPx
    val bitmapWidth = ceil(cardWidth).toInt()
    val bitmapHeight = ceil(cardHeight).toInt()
    if (bitmapWidth <= 0 || bitmapHeight <= 0) return@drawWithCache onDrawBehind { }

    // Shared by every card of this cloud.
    val cloudRandom = Random(cloudSegment.cloudIndex xor sessionSeed)
    val averageBumpStepPx = (cloudRandom.nextFloat() * BUMP_STEP_DP_RANGE + BUMP_STEP_DP_MIN).dp.toPx()
    val deepestX = cloudRandom.nextFloat() * CLOUD_DEEPEST_X_RANGE + CLOUD_DEEPEST_X_MIN
    val entranceT = cloudRandom.nextFloat() * CLOUD_EDGE_INSET_MAX
    val exitT = 1f - cloudRandom.nextFloat() * CLOUD_EDGE_INSET_MAX
    // This card's own bumps.
    val cardRandom = Random(cloudSegment.cloudIndex * CLOUD_SEGMENT_COUNT + cloudSegment.segment xor sessionSeed)
    val strokeWidthPx = STROKE_WIDTH.toPx()

    // Centerline x at a y within the card (card coordinates, LTR). t runs 0..1 across the whole
    // cloud; u runs -1..1 between the entrance and the exit, with the deepest point at 0 - outside
    // that range the line stays off-screen at CLOUD_OUTER_X.
    fun centerX(y: Float): Float {
        val t = (cloudSegment.segment + y / cardHeight) / CLOUD_SEGMENT_COUNT
        val u = (2f * (t - entranceT) / (exitT - entranceT) - 1f).coerceIn(-1f, 1f)
        val bulge = (1f - u * u).pow(CLOUD_ROUNDNESS_EXPONENT) // 1 at the deepest point, 0 at the ends
        return cardWidth * (CLOUD_OUTER_X - (CLOUD_OUTER_X - deepestX) * bulge)
    }

    val bumpPoints = bumpPoints(
        cardHeight = cardHeight,
        sampleStepPx = CURVE_SAMPLE_STEP.toPx(),
        averageBumpStepPx = averageBumpStepPx,
        random = cardRandom,
        centerX = ::centerX
    )
    val railSpacing = strokeWidthPx / (RAIL_COUNT - 1)
    val rails = List(RAIL_COUNT) { rail ->
        // Rail 0 is the outer (start-side) edge - the opaque one.
        val offset = strokeWidthPx / 2f - rail * railSpacing
        Path().apply {
            val first = bumpPoints.first().let { it.position + it.normal * offset }
            moveTo(first.x, first.y)
            for (i in 1 until bumpPoints.size) {
                val chord = (bumpPoints[i].position - bumpPoints[i - 1].position).getDistance()
                addArcBetween(
                    path = this,
                    p0 = bumpPoints[i - 1].let { it.position + it.normal * offset },
                    p1 = bumpPoints[i].let { it.position + it.normal * offset },
                    radius = chord / bumpPoints[i].radiusFactor
                )
            }
        }
    }

    val bitmap = ImageBitmap(bitmapWidth, bitmapHeight)
    val transparentColor = color.copy(alpha = 0f)
    val railWidthPx = railSpacing * 1.5f // slight overlap, no seams
    CanvasDrawScope().draw(this, layoutDirection, Canvas(bitmap), Size(cardWidth, cardHeight)) {
        // Built for LTR; mirroring keeps "start side" and "end side" right under RTL.
        scale(scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f, scaleY = 1f) {
            rails.forEachIndexed { rail, path ->
                drawPath(
                    path = path,
                    color = lerp(color, transparentColor, rail / (RAIL_COUNT - 1).toFloat()),
                    alpha = LINE_ALPHA,
                    style = Stroke(width = railWidthPx, cap = StrokeCap.Round)
                )
            }
        }
    }

    onDrawBehind {
        drawImage(bitmap, topLeft = Offset(-bleedPx, -bleedPx))
    }
}

// [radiusFactor] shapes the bump that ends at this point (unused on the first point).
private class BumpPoint(val position: Offset, val normal: Offset, val radiusFactor: Float)

// Bump endpoints from the card's bottom edge up to its top edge: a whole number of bumps along the
// curve's length (not its height - the cloud runs nearly sideways near its ends, where equal height
// steps would stretch the bumps), averaging [averageBumpStepPx] but each randomly longer or shorter.
// Both edges get an endpoint, so bumps join up across cards. Each point carries the unit normal
// pointing to the start side of travel; climbing upward is what makes addArcBetween bulge toward
// the start.
private fun bumpPoints(
    cardHeight: Float,
    sampleStepPx: Float,
    averageBumpStepPx: Float,
    random: Random,
    centerX: (Float) -> Float
): List<BumpPoint> {
    val sampleCount = (cardHeight / sampleStepPx).toInt().coerceAtLeast(1)
    val samples = List(sampleCount + 1) { i ->
        val y = cardHeight - cardHeight * i / sampleCount
        Offset(centerX(y), y)
    }
    val lengths = FloatArray(samples.size)
    for (i in 1 until samples.size) lengths[i] = lengths[i - 1] + (samples[i] - samples[i - 1]).getDistance()
    val totalLength = lengths.last()
    val bumpCount = (totalLength / averageBumpStepPx).roundToInt().coerceAtLeast(1)
    // Random relative lengths, scaled so they add up to exactly the card's length.
    val weights = List(bumpCount) { 1f + (random.nextFloat() * 2f - 1f) * BUMP_LENGTH_VARIATION }
    val weightSum = weights.sum()
    val targets = weights.runningFold(0f) { acc, weight -> acc + weight / weightSum * totalLength }

    var sample = 0
    return targets.map { target ->
        while (sample < samples.lastIndex - 1 && lengths[sample + 1] < target) sample++
        val before = samples[sample]
        val after = samples[(sample + 1).coerceAtMost(samples.lastIndex)]
        val span = lengths[(sample + 1).coerceAtMost(samples.lastIndex)] - lengths[sample]
        val fraction = if (span > 0f) ((target - lengths[sample]) / span).coerceIn(0f, 1f) else 0f
        val tangent = after - before
        val length = tangent.getDistance()
        // Rotated a quarter turn counter-clockwise (screen space): the start side when climbing.
        val normal = if (length > 0f) Offset(tangent.y / length, -tangent.x / length) else Offset(-1f, 0f)
        BumpPoint(
            position = before + (after - before) * fraction,
            normal = normal,
            radiusFactor = random.nextFloat() * BUMP_RADIUS_FACTOR_RANGE + BUMP_RADIUS_FACTOR_MIN
        )
    }
}

// Adds a minor circular arc from p0 to p1 with the given radius, always bulging to the same side
// (SVG's large-arc-flag=0, sweep-flag=1 equivalent) - Compose's Path has no direct two-endpoint
// arc command, so the center is found the same way an SVG arc's is: the chord's perpendicular
// bisector, offset by the leg of the right triangle formed with the radius.
private fun addArcBetween(path: Path, p0: Offset, p1: Offset, radius: Float) {
    val dx = (p1.x - p0.x) / 2f
    val dy = (p1.y - p0.y) / 2f
    val halfChord = hypot(dx, dy)
    if (halfChord == 0f) return
    val r = radius.coerceAtLeast(halfChord)
    val h = sqrt((r * r - halfChord * halfChord).coerceAtLeast(0f))
    val midX = (p0.x + p1.x) / 2f
    val midY = (p0.y + p1.y) / 2f
    val perpX = -dy / halfChord
    val perpY = dx / halfChord
    val centerX = midX + h * perpX
    val centerY = midY + h * perpY

    val startAngle = atan2(p0.y - centerY, p0.x - centerX)
    var sweep = atan2(p1.y - centerY, p1.x - centerX) - startAngle
    if (sweep < 0) sweep += (2 * PI).toFloat()

    path.arcTo(
        rect = Rect(centerX - r, centerY - r, centerX + r, centerY + r),
        startAngleDegrees = startAngle * (180f / PI.toFloat()),
        sweepAngleDegrees = sweep * (180f / PI.toFloat()),
        forceMoveTo = false
    )
}
