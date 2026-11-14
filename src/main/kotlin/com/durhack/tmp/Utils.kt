package com.durhack.tmp

import org.openrndr.Program
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.random.Random

val AntRandom = Random(1)

val Program.centre: Vector2
    get() = Vector2(width / 2.0, height / 2.0)

fun Vector2.Companion.random(scale: Double): Vector2 {
    return Vector2(
        AntRandom.nextDouble(-1.0, 1.0) * scale,
        AntRandom.nextDouble(-1.0, 1.0) * scale
    )
}

fun Vector2.angleTowards(other: Vector2): Double {
    val d = other - this
    return atan2(d.y, d.x)
}

fun Vector2(x: Int, y: Int): Vector2
{
    return Vector2(x.toDouble(), y.toDouble())
}

fun Vector2(p: IntVector2): Vector2
{
    return Vector2(p.x, p.y)
}

fun nearestIntPoint(p: Vector2): IntVector2
{
    return IntVector2(p.x.roundToInt(), p.y.roundToInt())
}

fun min(p0: IntVector2, p1: IntVector2): IntVector2
{
    return IntVector2(
        min(p0.x, p1.x),
        min(p0.y, p1.y)
    )
}

fun IntRectangle.offsetEdges(xOffset: Int, yOffset: Int): IntRectangle
{
    var corner = this.corner - IntVector2(xOffset, yOffset)
    var width = this.width + xOffset * 2
    var height = this.height + yOffset * 2
    return IntRectangle(corner, width, height)
}

fun DoubleArray.weightedIndex(random: Random = AntRandom): Int {
    val sum = sum()
    if (sum == 0.0) {
        return random.nextInt(this.size)
    }

    var r = random.nextDouble() * sum
    for (i in indices) {
        r -= this[i]
        if (r < 0) return i
    }
    return indexOfLast { it > 0.0 }
}

fun degreesBetween(p0: Double, p1: Double): Double {
    return wrapDegrees(p1 - p0)
}

fun wrapDegrees(angle: Double): Double {
    var normalizedAngle = angle % 360.0
    if (normalizedAngle >= 180.0) {
        normalizedAngle -= 360.0
    } else if (normalizedAngle < -180.0) {
        normalizedAngle += 360.0
    }

    return normalizedAngle
}
