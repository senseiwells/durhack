package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.AntRandom
import com.durhack.tmp.degreesBetween
import com.durhack.tmp.weightedIndex
import org.openrndr.math.Polar
import kotlin.math.abs

class AdventuringAction(
    override val ant: Ant
): Action {
    private var ticks = 0

    init {
        this.ant.dir = AntRandom.nextDouble() * 360
    }

    override fun act(): Action {
        if (this.ticks++ > 1_000) {
            return ReturningAction(this.ant)
        }

        val smellyWeights = DoubleArray(SAMPLE_POINTS) { i ->
            val probe = this.ant.pos + Polar(360.0 / SAMPLE_POINTS * i, this.ant.traits.speed).cartesian
            this.ant.colony.pheromone.howSmelly(probe)
        }

        val index = smellyWeights.weightedIndex()

        val newDir = 360.0 / SAMPLE_POINTS * index
        val desiredTurn = degreesBetween(this.ant.dir, newDir)
        val actualTurn = desiredTurn.coerceIn(-CLAMPING_ANGLE, CLAMPING_ANGLE)
        this.ant.dir += actualTurn

        this.ant.move(1 - (abs(actualTurn - desiredTurn) / 180))

        val nearest = this.ant.world.getNearestFood(this.ant.pos)
        if (nearest.pos.distanceTo(this.ant.pos) < 10.0) {
            return GatheringAction(this.ant, nearest)
        }

        return this
    }

    companion object {
        const val SAMPLE_POINTS = 12
        const val CLAMPING_ANGLE = 10.0
    }
}
