package com.durhack.tmp

import org.openrndr.math.Vector2

class Food(
    val pos: Vector2,
    var remaining: Int = 200
) {
    val isDepleted: Boolean get() = this.remaining <= 0
}