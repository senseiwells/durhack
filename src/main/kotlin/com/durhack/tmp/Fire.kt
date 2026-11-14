package com.durhack.tmp

import org.openrndr.math.Vector2

class Fire(
    val pos: Vector2,
    val radius: Double
) {
    fun areYouOnFire(pos: Vector2): Boolean {
        return pos.closerThan(this.pos, this.radius)
    }
}