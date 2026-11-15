package com.durhack.tmp

import com.durhack.tmp.rendering.AntRenderState
import com.durhack.tmp.rendering.CasinoRenderState
import com.durhack.tmp.rendering.ColonyRenderState
import com.durhack.tmp.rendering.FoodRenderState
import com.durhack.tmp.rendering.ObstacleRenderState
import com.durhack.tmp.rendering.WorldRenderState
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Vector2

class World {
    val colonies = mutableListOf<Colony>()
    val ants = mutableListOf<Ant>()
    val food = mutableListOf<Food>()
    val obstacles = mutableListOf<Fire>()
    val casinos = mutableListOf<Casino>()

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

    fun createCasino(pos: Vector2) {
        val casino = Casino(pos)
        this.casinos.add(casino)
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

    fun getNearestCasino(pos: Vector2): Casino? {
        return this.casinos.minByOrNull { casino -> casino.pos.squaredDistanceTo(pos) }
    }

    fun getNearestFood(pos: Vector2): Food? {
        return this.food.minByOrNull { food -> food.pos.squaredDistanceTo(pos) }
    }

    fun checkIfImOnFire(pos: Vector2): Boolean {
        return this.obstacles.any { obstacle -> obstacle.areYouOnFire(pos) }
    }

    fun tick() {
        this.ticks++

        for (colony in this.colonies) {
            colony.pheromone.evaporate()
            colony.pheromone.makeCache()
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
            this.ants.map { ant -> AntRenderState(ant.pos, ant.dir, ant.color(), ant.visible) },
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
            this.casinos.map { casino -> CasinoRenderState(casino.pos) }
        )
    }
}