package com.durhack.tmp

import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import kotlin.math.exp

data class TrailPoint(
    val pos: Vector2,
    var strength: Double
)

typealias Trail = MutableList<TrailPoint>

private const val CACHE_WIDTH = 500
private const val CACHE_HEIGHT = 500
class PheromoneMap {
    var nextTrailId = 0
    var trails = mutableMapOf<Int, Trail> ()
    var cache = DoubleArray(CACHE_WIDTH * CACHE_HEIGHT)

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
        var numPoints = 0
        for (trail in trails.values)
            numPoints += trail.size

        println("Num points: ${numPoints}")

        for (trail in trails.values)
            for (p in trail)
                p.strength *= 0.99
    }

    fun makeCache()
    {

    }

    fun getBounding(): Rectangle
    {
        val x = mutableListOf<Vector2>()
        x.bounds
    }
}