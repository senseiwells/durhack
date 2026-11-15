package com.durhack.tmp

import com.durhack.tmp.rendering.TripleBuffer
import kotlinx.serialization.Serializable
import org.openrndr.math.IntVector2
import org.openrndr.math.Vector2
import org.openrndr.shape.IntRectangle
import org.openrndr.shape.Rectangle
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

@Serializable
data class TrailPoint(
    val pos: Vector2,
    var strength: Double
)

data class CachePoint(
    var trailEpoch: Int,
    var strength: Double
)
typealias PheromoneTrail = MutableList<TrailPoint>

const val CACHE_WIDTH = 500
private const val CACHE_HEIGHT = 500
private const val RADIUS = 5.0
private const val CLEANUP_THRESH = 1e-2

class PheromoneMap {
    var trails = mutableListOf<PheromoneTrail>()
    var cache = TripleBuffer(CACHE_WIDTH * CACHE_HEIGHT)
    var cacheBounds = Rectangle(Vector2(0.0, 0.0), 1.0, 1.0)

    fun createTrail(): PheromoneTrail
    {
        val trail = mutableListOf<TrailPoint>()
        this.trails.add(trail)
        return trail
    }

    fun makePointSmell(trail: PheromoneTrail, pos: Vector2, pungency: Double = 1.0)
    {
        trail.add(TrailPoint(pos, pungency))
    }

    fun howSmelly(pos: Vector2): Double
    {
        val cPos = worldPointToCache(pos)
        if (cPos.x !in 0..<CACHE_WIDTH) return 0.0
        if (cPos.y !in 0..<CACHE_HEIGHT) return 0.0
        return cache.writeBuffer[cPos.y * CACHE_WIDTH + cPos.x].strength
    }

    fun evaporate()
    {
        for (trail in trails)
            for (p in trail)
                p.strength *= 0.993
        cleanupTrails()
    }

    private fun cleanupTrails()
    {
        for (trail in trails)
            trail.removeIf { point -> (point.strength < CLEANUP_THRESH) }

        trails.removeIf { trail -> trail.isEmpty() }
    }

    fun makeCache()
    {
        for (i in 0..<(CACHE_WIDTH * CACHE_HEIGHT)) cache.writeBuffer[i] = CachePoint(-1, 0.0)

        // Update cacheBounds
        if (this.trails.sumOf { it.size } <= 0) return
        cacheBounds = getBounds()
        cacheBounds = cacheBounds.offsetEdges(RADIUS * 2)

        for (trailIdx in trails.indices)
            depositTrail(trailIdx)
    }

    // Returns 'true' if flooding should continue
    fun updateCachePoint(cp: IntVector2, trailIdx: Int, trailPointIdx: Int): Boolean
    {
        // Lookup cache point and check if it was reached earlier
        val cachePoint = cache.writeBuffer[cp.y * CACHE_WIDTH + cp.x]
        if (trailIdx == cachePoint.trailEpoch) return false

        // Update trailEpoch
        cachePoint.trailEpoch = trailIdx

        // Calculate strength
        var dist = Double.POSITIVE_INFINITY
        val p1 = trails[trailIdx][trailPointIdx]
        val worldPos = cachePointToWorld(cp)
        if (trailPointIdx > 0)
        {
            val p0 = trails[trailIdx][trailPointIdx - 1]
            dist = min(dist, distToLine(p0.pos, p1.pos, worldPos))
        }
        if (trailPointIdx < trails[trailIdx].size - 1)
        {
            val p2 = trails[trailIdx][trailPointIdx + 1]
            dist = min(dist, distToLine(p1.pos, p2.pos, worldPos))
        }
        val newStrength = p1.strength / exp(dist * 2.0)

        // Update point strength
        val isBetter = (newStrength > cachePoint.strength)
        cachePoint.strength = max(cachePoint.strength, newStrength)

        return isBetter && newStrength > CLEANUP_THRESH
    }

    fun publishCache() {
        cache.publish()
    }

    fun depositTrail(trailIdx: Int)
    {
        // Build a frontier around the trail
        var frontier = mutableListOf<Pair<IntVector2, Int>>()
        for (trailPointIdx in trails[trailIdx].indices)
        {
            val cp = worldPointToCache(trails[trailIdx][trailPointIdx].pos)
            for (dy in 0..1)
            {
                for (dx in 0..1)
                {
                    val cornerPos = cp + IntVector2(dx, dy)
                    val shouldFlood = updateCachePoint(cornerPos, trailIdx, trailPointIdx)
                    if (shouldFlood) frontier.add(Pair(cornerPos, trailPointIdx))
                }
            }
        }

        // Expand the frontier until pheromone until flooding stops
        val deltas = listOf(IntVector2(-1, 0), IntVector2(1, 0), IntVector2(0, -1), IntVector2(0, 1))
        do
        {
            val newFrontier = mutableListOf<Pair<IntVector2, Int>>()
            for ((cp, trailPointIdx) in frontier)
            {
                for (delta in deltas)
                {
                    val newPoint = cp + delta
                    val shouldFlood = updateCachePoint(newPoint, trailIdx, trailPointIdx)
                    if (shouldFlood) newFrontier.add(Pair(newPoint, trailPointIdx))
                }
            }

            frontier = newFrontier
        } while (frontier.isNotEmpty())
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
        return floorIntPoint(cPoint)
    }

    private fun getBounds(): Rectangle
    {
        var minX = Double.POSITIVE_INFINITY
        var minY = Double.POSITIVE_INFINITY
        var maxX = Double.NEGATIVE_INFINITY
        var maxY = Double.NEGATIVE_INFINITY

        for (trail in trails)
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