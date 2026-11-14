package com.durhack.tmp

import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Rectangle
import org.openrndr.shape.bounds
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

data class TrailPoint(
    val pos: Vector2,
    var strength: Double
)

typealias Trail = MutableList<TrailPoint>

private const val CACHE_WIDTH = 500
private const val CACHE_HEIGHT = 500
private const val RADIUS = 5.0

class PheromoneMap {
    var nextTrailId = 0
    var trails = mutableMapOf<Int, Trail> ()
    var cache = DoubleArray(CACHE_WIDTH * CACHE_HEIGHT)
    var cacheBounds = Rectangle(Vector2(0.0, 0.0), 1.0, 1.0)

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
        val cPos = worldPointToCache(pos)
        if (cPos.x !in 0..<CACHE_WIDTH) return 0.0
        if (cPos.y !in 0..<CACHE_HEIGHT) return 0.0
        return cache[cPos.y * CACHE_WIDTH + cPos.x]
    }

    fun evaporate()
    {
        for (trail in trails.values)
            for (p in trail)
                p.strength *= 0.95
    }

    fun makeCache()
    {
        cache.fill(0.0)

        cacheBounds = getBounds()
        if (cacheBounds.corner.x.isInfinite()) cacheBounds = Rectangle(Vector2(0.0, 0.0), 1.0, 1.0)
        cacheBounds = cacheBounds.offsetEdges(RADIUS * 2)

        for (trail in trails.values)
            for ((p0, p1) in trail.zipWithNext())
                depositSegment(p0, p1)
    }

    private fun cachePointToWorld(cPoint: IntVector2): Vector2
    {
        var wPoint = Vector2(cPoint)
        wPoint /= Vector2(CACHE_WIDTH - 1, CACHE_HEIGHT - 1)
        wPoint *= cacheBounds.dimensions
        wPoint += cacheBounds.corner
        return wPoint
    }

    private fun worldPointToCache(wPoint: Vector2): IntVector2
    {
        var cPoint = wPoint
        cPoint -= cacheBounds.corner
        cPoint /= cacheBounds.dimensions
        cPoint *= Vector2(CACHE_WIDTH - 1, CACHE_HEIGHT - 1)
        return nearestIntPoint(cPoint)
    }

    private fun depositSegment(p0: TrailPoint, p1: TrailPoint)
    {
        val strength = (p0.strength + p1.strength) * 0.5

        val cp0 = worldPointToCache(p0.pos)
        val cp1 = worldPointToCache(p1.pos)
        val spacing = cacheBounds.dimensions / Vector2(CACHE_WIDTH - 1, CACHE_HEIGHT - 1)
        val cRadius = Vector2(RADIUS) / spacing

        var cLineBounds = IntRectangle(min(cp0, cp1), abs(cp1.x - cp0.x), abs(cp1.y - cp0.y))
        cLineBounds = cLineBounds.offsetEdges(ceil(cRadius.x).toInt(), ceil(cRadius.y).toInt())

        for (dy in 0..<cLineBounds.height)
        {
            for (dx in 0..<cLineBounds.width)
            {
                val cx = cLineBounds.corner.x + dx
                val cy = cLineBounds.corner.y + dy

                val worldPos = cachePointToWorld(IntVector2(cx, cy))
                val dist = distToLine(p0.pos, p1.pos, worldPos)
                val smell = strength / exp(dist * 2.0)
                cache[cy * CACHE_WIDTH + cx] += smell
            }
        }
    }

    private fun getBounds(): Rectangle
    {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY

        for (trail in trails.values)
        {
            for (trailPoint in trail)
            {
                val p = trailPoint.pos
                minX = min(minX, p.x)
                maxX = max(maxX, p.x)
                minY = min(minY, p.y)
                maxY = max(maxY, p.y)
            }
        }

        return Rectangle(minX, minY, maxX - minX, maxY - minY)
    }

    private fun distToLine(start: Vector2, end: Vector2, target: Vector2): Double {
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