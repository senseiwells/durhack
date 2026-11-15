package com.durhack.tmp.rendering

class WorldRenderState(
    val ants: List<AntRenderState> = listOf(),
    val colonies: List<ColonyRenderState> = listOf(),
    val food: List<FoodRenderState> = listOf(),
    val obstacles: List<ObstacleRenderState> = listOf(),
)