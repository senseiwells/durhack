package com.durhack.tmp

import org.openrndr.math.Vector2

class Food(
    val pos: Vector2,
    var remaining: Int = 100
) {
    val isDepleted: Boolean get() = this.remaining <= 0
}