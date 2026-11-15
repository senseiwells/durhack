package com.durhack.tmp

import com.durhack.tmp.bleeding_edge.PheromoneMap2
import org.openrndr.math.Vector2

class Colony(
    val pos: Vector2,
) {
    val pheromone = PheromoneMap2()
}