package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.angleTowards

class ReturningAction(
    override val ant: Ant
): Action {
    init {
        this.ant.dir = Math.toDegrees(this.ant.pos.angleTowards(this.ant.colony.pos))
    }

    override fun act(): Action {
        this.ant.move()

        if (this.ant.pos.distanceTo(this.ant.colony.pos) < 5.0) {
            return RestingAction(this.ant)
        }
        return this
    }
}