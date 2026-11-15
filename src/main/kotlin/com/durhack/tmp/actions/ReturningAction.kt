package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.AntLogger
import com.durhack.tmp.CoreSettings
import com.durhack.tmp.PheromoneTrail
import com.durhack.tmp.angleTowards
import com.durhack.tmp.closerThan
import com.durhack.tmp.wrapDegrees
import org.openrndr.math.asDegrees
import kotlin.math.max
import kotlin.math.pow

class ReturningAction(
    override val ant: Ant
): Action {
    private lateinit var pheromoneTrail: PheromoneTrail

    private val returningPathLength = this.ant.adventuringTrail.zipWithNext().sumOf { (a, b) -> a.distanceTo(b) }
    private val optimalPathLength = this.ant.pos.distanceTo(this.ant.adventuringTrail.first())
    private val strength = (100.0 / this.returningPathLength).pow(1.5)

    init {
        this.ant.dir = Math.toDegrees(this.ant.pos.angleTowards(this.ant.colony.pos))

        if (this.ant.food > 0) {
            this.pheromoneTrail = this.ant.colony.pheromone.createTrail()
            this.makeAntFart()
        }
    }

    override fun act(): Action {
        var targetIndex = this.ant.adventuringTrail.indexOfFirst { pos ->
            this.ant.pos.closerThan(pos, this.ant.traits.speed * CoreSettings.pathSmoothing)
        }
        if (targetIndex == -1) {
            AntLogger.error { "Not on trail>?>!>!>??!?" }
        }

        targetIndex = max(0, targetIndex - 1)

        val target = this.ant.adventuringTrail[targetIndex]
        this.ant.dir = this.ant.pos.angleTowards(target).asDegrees

        this.ant.move()
        if (this::pheromoneTrail.isInitialized) {
            this.makeAntFart()
        }

        if (this.ant.pos.distanceTo(this.ant.colony.pos) < 8.0) {
            this.ant.onReturnToColony()
            this.ant.dir = wrapDegrees(this.ant.dir + 180)
            return RestingAction(this.ant)
        }
        return this
    }

    private fun makeAntFart() {
        // var strength = 100 / this.returningPathLength
        // strength = strength.pow(1.5)
        this.ant.colony.pheromone.makePointSmell(this.pheromoneTrail, ant.pos, strength)
    }
}