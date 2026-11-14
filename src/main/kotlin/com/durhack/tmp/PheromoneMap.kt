package com.durhack.tmp

import org.openrndr.math.Vector2
import kotlin.math.exp

data class TrailPoint(
    val pos: Vector2,
    var strength: Double
)

typealias Trail = MutableList<TrailPoint>

class PheromoneMap {
    var nextTrailId = 0
    var trails: MutableMap<Int, Trail> = mutableMapOf()

    fun createTrail(): Int
    {
        val trailId = nextTrailId++
        trails[trailId] = mutableListOf()
        return trailId
    }

    fun makePointSmell(trailId: Int, pos: Vector2)
    {
        trails[trailId]!!.add(TrailPoint(pos, 1.0))
    }

    fun howSmelly(pos: Vector2): Double
    {
        var smell = 0.0

        for (trail in trails.values)
        {
            for ((p0, p1) in trail.zipWithNext())
            {
                val strength = (p0.strength + p1.strength) / 2.0
                val dist = distToLine(p0.pos, p1.pos, pos)
                smell += strength / exp(dist)
            }
        }

        return smell
    }

    fun evaporate()
    {
        for (trail in trails.values)
            for (p in trail)
                p.strength *= 0.9
    }

    fun distToLine(start: Vector2, end: Vector2, target: Vector2): Double {
        val line = end - start
        val lenSq = line.squaredLength
        if (lenSq == 0.0) return (target - start).length

        val factor = (target - start).dot(line) / lenSq
        if (factor < 0) return (target - start).length
        if (factor > 1) return (target - end).length

        val closest = start + line * factor
        return (target - closest).length
    }
}