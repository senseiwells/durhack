package com.durhack.tmp

import org.openrndr.Program
import org.openrndr.math.Vector2
import kotlin.math.atan2
import kotlin.random.Random

val Program.centre: Vector2
    get() = Vector2(width / 2.0, height / 2.0)

fun Vector2.Companion.random(scale: Double): Vector2 {
    return Vector2(
        Random.nextDouble(-1.0, 1.0) * scale,
        Random.nextDouble(-1.0, 1.0) * scale
    )
}

fun Vector2.angleTowards(other: Vector2): Double {
    val d = other - this
    return atan2(d.y, d.x)
}