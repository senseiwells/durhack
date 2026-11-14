package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import kotlin.random.Random

class AdventuringAction(
    override val ant: Ant
): Action {
    var trailId: Int

    init {
        this.ant.dir = Random.nextDouble() * 360

        trailId = ant.colony.pheromone.createTrail()
        ant.colony.pheromone.makePointSmell(trailId, ant.pos)
    }

    override fun act(): Action {
        this.ant.move()
        ant.colony.pheromone.makePointSmell(trailId, ant.pos)

        val nearest = this.ant.world.getNearestFood(this.ant.pos)
        if (nearest.pos.distanceTo(this.ant.pos) < 10.0) {
            return GatheringAction(this.ant, nearest)
        }

        return this
    }
}
