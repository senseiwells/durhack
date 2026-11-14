package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import kotlin.random.Random

class AdventuringAction(
    override val ant: Ant
): Action {
    init {
        this.ant.dir = Random.nextDouble() * 360
    }

    override fun act(): Action {
        this.ant.move()

        val nearest = this.ant.world.getNearestFood(this.ant.pos)
        if (nearest.pos.distanceTo(this.ant.pos) < 10.0) {
            return GatheringAction(this.ant, nearest)
        }
        return this
    }
}
