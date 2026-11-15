package com.durhack.tmp

import com.durhack.tmp.rendering.AntRenderState
import com.durhack.tmp.rendering.ColonyRenderState
import com.durhack.tmp.rendering.FoodRenderState
import com.durhack.tmp.rendering.ObstacleRenderState
import com.durhack.tmp.rendering.WorldRenderState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2
import java.nio.file.Path
import java.util.IdentityHashMap
import kotlin.io.path.outputStream
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class World {
    val colonies = mutableListOf<Colony>()
    val ants = mutableListOf<Ant>()
    val food = mutableListOf<Food>()
    val obstacles = mutableListOf<Fire>()
    var savedTrail = false

    var ticks = 0

    var renderState = WorldRenderState()

    fun createColony(pos: Vector2, color: ColorRGBa): Colony {
        val colony = Colony(pos, color)
        this.colonies.add(colony)
        return colony
    }

    fun createAnt(colony: Colony): Ant {
        val ant = Ant.create(this, colony)
        this.ants.add(ant)
        return ant
    }

    fun createFood(pos: Vector2) {
        val food = Food(pos, CoreSettings.foodAmount)
        this.food.add(food)
    }

    fun createRandomFood(foodPerColony: Int = 1) {
        for (colony in this.colonies) {
            repeat(foodPerColony) {
                val offset = Vector2.random(CoreSettings.foodRandomDistance.toDouble())
                this.createFood(colony.pos + offset)
            }
        }
    }

    fun createObstaclesBetweenColoniesAndFood() {
        for (colony in this.colonies) {
            for (food in this.food) {
                val direction = food.pos - colony.pos
                val fire = Fire(colony.pos + direction / 2.0, AntRandom.nextDouble(5.0, 8.0))
                this.obstacles.add(fire)
            }
        }
    }

    fun getNearestFood(pos: Vector2): Food {
        return this.food.minBy { food -> food.pos.squaredDistanceTo(pos) }
    }

    fun checkIfImOnFire(pos: Vector2): Boolean {
        return this.obstacles.any { obstacle -> obstacle.areYouOnFire(pos) }
    }

    fun tick() {
        this.ticks++

        for (colony in this.colonies) {
            colony.pheromone.evaporate()
            val elapsed = measureTimeMillis { colony.pheromone.makeCache() }
            // if (elapsed > 100.0 && !savedTrail)
            // {
            //     val path = Path.of("./trails.json")
            //     Json.encodeToStream(colonies.first().pheromone.trails, path.outputStream())
            //     AntLogger.info { "Saving trails" }
            //     savedTrail = true
            // }
        }

        this.ants.forEach(Ant::tick)
        this.ants.removeIf(Ant::dead)

        this.food.removeIf(Food::isDepleted)
        if (this.food.isEmpty()) {
            this.createRandomFood(CoreSettings.food)
        }

        for (colony in this.colonies) {
            colony.pheromone.publishCache()
        }
        this.extractRenderState()
    }

    private fun extractRenderState() {
        this.renderState = WorldRenderState(
            this.ants.map { ant -> AntRenderState(ant.pos, ant.color(), ant.visible) },
            this.colonies.map { colony ->
                val trails = colony.pheromone.trails.map { trail ->
                    trail.asSequence().filterIndexed { i, _ -> i % 10 == 0 }
                        .map { it.pos }
                        .zipWithNext()
                        .toList()
                }
                ColonyRenderState(colony.pos, colony.color, trails, colony.pheromone.cache)
            },
            this.food.map { food -> FoodRenderState(food.pos) },
            this.obstacles.map { obstacle -> ObstacleRenderState(obstacle.pos, obstacle.radius) },
        )
    }
}