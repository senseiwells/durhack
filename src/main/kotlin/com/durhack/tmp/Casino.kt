package com.durhack.tmp

import org.openrndr.math.Vector2
import kotlin.math.max
import kotlin.math.min

class Casino(
    val pos: Vector2
) {
    fun canScam(ant: Ant): Boolean {
        return ant.cashmoney > 0
    }

    fun scam(ant: Ant): Boolean {
        if (!this.canScam(ant)) {
            // you too broke for the casino
            return false
        }

        // they take out a loan if they're borderline broke
        val wager = max(10, ant.cashmoney / 2)
        if (AntRandom.nextDouble() < 0.45) {
            ant.cashmoney += wager
        } else {
            ant.cashmoney -= wager
        }
        return true
    }
}