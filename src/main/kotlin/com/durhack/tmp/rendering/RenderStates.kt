package com.durhack.tmp.rendering

import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

class AntRenderState(
    val pos: Vector2,
    val dir: Double,
    val color: ColorRGBa,
    val visible: Boolean,
)

class ColonyRenderState(
    val pos: Vector2,
    val color: ColorRGBa,
    val trails: List<List<Pair<Vector2, Vector2>>>,
    val buffer: TripleBuffer,
)

class FoodRenderState(
    val pos: Vector2
)

class ObstacleRenderState(
    val pos: Vector2,
    val radius: Double
)