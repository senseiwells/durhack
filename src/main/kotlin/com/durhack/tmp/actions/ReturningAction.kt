package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.Trail
import com.durhack.tmp.angleTowards

class ReturningAction(
    override val ant: Ant
): Action {
    private lateinit var trail: Trail

    init {
        this.ant.dir = Math.toDegrees(this.ant.pos.angleTowards(this.ant.colony.pos))

        if (this.ant.food > 0) {
            this.trail = this.ant.colony.pheromone.createTrail()
            this.ant.colony.pheromone.makePointSmell(this.trail, ant.pos)
        }
    }

    override fun act(): Action {
        this.ant.move()
        if (this::trail.isInitialized) {
            this.ant.colony.pheromone.makePointSmell(this.trail, ant.pos)
        }

        if (this.ant.pos.distanceTo(this.ant.colony.pos) < 5.0) {
            this.ant.food = 0
            return RestingAction(this.ant)
        }
        return this
    }
}