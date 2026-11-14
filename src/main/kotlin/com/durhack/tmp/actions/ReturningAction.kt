package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.angleTowards

class ReturningAction(
    override val ant: Ant
): Action {
    private var trailId = 0

    init {
        this.ant.dir = Math.toDegrees(this.ant.pos.angleTowards(this.ant.colony.pos))

        if (this.ant.food > 0) {
            this.trailId = this.ant.colony.pheromone.createTrail()
            this.ant.colony.pheromone.makePointSmell(trailId, ant.pos)
        }

    }

    override fun act(): Action {
        this.ant.move()
        if (this.ant.food > 0) {
            this.ant.colony.pheromone.makePointSmell(trailId, ant.pos)
        }

        if (this.ant.pos.distanceTo(this.ant.colony.pos) < 5.0) {
            this.ant.food = 0
            return RestingAction(this.ant)
        }
        return this
    }
}