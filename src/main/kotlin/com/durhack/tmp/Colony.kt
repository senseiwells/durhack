package com.durhack.tmp

import org.openrndr.math.Vector2

class Colony(
    val pos: Vector2,
) {
    val id = COLONY_COUNT++

    val pheromone = PheromoneMap()

    companion object {
        var COLONY_COUNT = 0
    }
}