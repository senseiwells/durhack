package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import org.openrndr.math.Polar
import kotlin.random.Random

class AdventuringAction(
    override val ant: Ant
): Action {
    val dir = Random.nextDouble() * 360

    override fun act(): Action {
        this.ant.pos += Polar(this.dir, this.ant.traits.speed).cartesian
        return this
    }
}
