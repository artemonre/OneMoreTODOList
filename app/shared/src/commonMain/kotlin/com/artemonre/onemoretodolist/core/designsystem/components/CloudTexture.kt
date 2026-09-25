package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

// How far the line's dark/light edge is blended towards black/white to build its gradient's
// opaque end - a fraction (not absolute color) so it scales with however light or dark [color]
// itself already is.
private const val GRADIENT_SHADE_FRACTION = 0.55f
private val STROKE_WIDTH = 3.dp

// Compose has no "shade a Path across its own local width" primitive - a Brush is positioned in
// canvas space, not relative to the path, so it can't follow a curve. Instead this draws the same
// curve [RAIL_COUNT] times, each copy shifted a little further along the perpendicular (translating
// every point by the same vector reproduces the exact same curve, just offset - no path-offsetting
// math needed), each a solid color interpolated from opaque to transparent. Overlapping translucent
// copies read as one smooth gradient that hugs the line's own shape everywhere, instead of a flat
// gradient that drifts in and out of alignment with a wandering/curved path.
private const val RAIL_COUNT = 6

// Mixed into every card's seed below so the whole set of textures reshuffles on each fresh app
// process instead of being permanently pinned to each todo's id - initialized once per process
// (top-level property), so it's the same for every card drawn during this launch.
private val sessionSeed = Random.nextInt()

// A row of overlapping circular arcs climbing the full height of whatever this is applied to, at
// a 35-50deg angle off vertical - a simple "child's cloud doodle" texture, not a smooth curve.
// [seed] (e.g. a todo's id.hashCode()) makes each card's arc count, radius, angle, and starting
// point (10%-50% of the width, left to right) unique but stable within a single app launch - the
// same card always redraws identically across recompositions instead of reshuffling every frame,
// but the whole set looks different again next time the app is started (see [sessionSeed]). The
// line itself is shaded across its own width (not its length, and consistently along its whole
// length - see [RAIL_COUNT]): opaque (tinted towards black, or towards white when [isDarkTheme],
// so the "raised" edge reads the same way under both themes) on its left edge, fading to fully
// transparent - blending into whatever is behind it - on its right. Draw-only: relies on the
// caller's own clip (a Card's shape, typically) to crop it at the real edges - it deliberately
// draws one extra arc past both the top and bottom so the line reads as continuing off-screen
// rather than starting/ending at the edge.
fun Modifier.cloudTexture(seed: Int, color: Color, isDarkTheme: Boolean): Modifier = drawBehind {
    val random = Random(seed xor sessionSeed)
    val angleRad = (random.nextFloat() * 15f + 35f) * (PI.toFloat() / 180f) // 35-50 deg off vertical

    // Choosing the arc count so it evenly divides the real height (rather than a fixed step size)
    // means the top and bottom edges always land exactly on an arc's midpoint - where its tangent
    // matches the overall diagonal - instead of on the sharp tangent break at an arc's endpoint,
    // regardless of this particular card's actual height.
    val desiredStepPx = (random.nextFloat() * 10f + 22f).dp.toPx()
    val arcCount = (size.height / desiredStepPx).roundToInt().coerceAtLeast(1)
    val stepPx = size.height / arcCount

    val xStep = stepPx * tan(angleRad)
    val chord = hypot(xStep, stepPx)
    val radius = chord / (random.nextFloat() * 0.3f + 1.3f) // shallow bump, not a tight semicircle
    val strokeWidthPx = STROKE_WIDTH.toPx()
    val alpha = random.nextFloat() * 0.2f + 0.3f

    // Perpendicular to the line's own climb direction (cos/sin of angleRad) - the axis every rail
    // below is offset along.
    val perpX = cos(angleRad)
    val perpY = sin(angleRad)
    val opaqueColor = if (isDarkTheme) lerp(color, Color.White, GRADIENT_SHADE_FRACTION) else lerp(color, Color.Black, GRADIENT_SHADE_FRACTION)
    val transparentColor = opaqueColor.copy(alpha = 0f)

    val baseX = size.width * (random.nextFloat() * 0.4f + 0.1f) // 10%-50% of the width, left to right
    val baseY = size.height + stepPx / 2f
    val railSpacing = strokeWidthPx / (RAIL_COUNT - 1)
    val railWidthPx = railSpacing * 1.5f // slight overlap so neighboring rails don't show a seam

    for (rail in 0 until RAIL_COUNT) {
        val t = rail / (RAIL_COUNT - 1).toFloat() // 0 = opaque edge, 1 = fully transparent edge
        val railOffset = -strokeWidthPx / 2f + rail * railSpacing
        var x = baseX + perpX * railOffset
        var y = baseY + perpY * railOffset
        val path = Path().apply { moveTo(x, y) }
        repeat(arcCount + 2) {
            val nextX = x + xStep
            val nextY = y - stepPx
            addArcBetween(path, Offset(x, y), Offset(nextX, nextY), radius)
            x = nextX
            y = nextY
        }
        drawPath(
            path = path,
            color = lerp(opaqueColor, transparentColor, t),
            alpha = alpha,
            style = Stroke(width = railWidthPx, cap = StrokeCap.Round)
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
