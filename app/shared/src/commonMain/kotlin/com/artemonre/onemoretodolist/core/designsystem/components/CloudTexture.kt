package com.artemonre.onemoretodolist.core.designsystem.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.random.Random

// A row of overlapping circular arcs climbing the full height of whatever this is applied to, at
// roughly a 40deg angle off vertical - a simple "child's cloud doodle" texture, not a smooth
// curve. [seed] (e.g. a todo's id.hashCode()) makes each card's arc count, radius and starting
// point unique but stable - the same card always redraws identically across recompositions
// instead of reshuffling every frame. Draw-only: relies on the caller's own clip (a Card's shape,
// typically) to crop it at the real edges - it deliberately draws one extra arc past both the top
// and bottom so the line reads as continuing off-screen rather than starting/ending at the edge.
fun Modifier.cloudTexture(seed: Int, color: Color): Modifier = drawBehind {
    val random = Random(seed)
    val angleRad = (random.nextFloat() * 12f + 34f) * (PI.toFloat() / 180f) // ~34-46 deg off vertical

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
    val strokeWidthPx = (random.nextFloat() * 0.7f + 1.3f).dp.toPx()
    val alpha = random.nextFloat() * 0.2f + 0.3f

    var x = size.width * random.nextFloat() * 0.3f
    var y = size.height + stepPx / 2f
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
        color = color,
        alpha = alpha,
        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
    )
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
