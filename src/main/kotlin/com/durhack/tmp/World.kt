package com.durhack.tmp

import org.openrndr.math.Vector2

class World {
    val colonies = mutableListOf<Colony>()
    val ants = mutableListOf<Ant>()
    val food = mutableListOf<Food>()
    val obstacles = mutableListOf<Fire>()

    var ticks = 0

    fun createColony(pos: Vector2): Colony {
        val colony = Colony(pos)
        this.colonies.add(colony)
        return colony
    }

    fun createAnt(colony: Colony): Ant {
        val ant = Ant.create(this, colony)
        this.ants.add(ant)
        return ant
    }

    fun createFood(pos: Vector2) {
        val food = Food(pos)
        this.food.add(food)
    }

    fun createRandomFood(foodPerColony: Int = 1) {
        for (colony in this.colonies) {
            repeat(foodPerColony) {
                this.createFood(colony.pos + Vector2.random(100.0))
            }
        }
    }

    fun createObstaclesBetweenColoniesAndFood() {
        for (colony in this.colonies) {
            for (food in this.food) {
                val direction = food.pos - colony.pos
                val fire = Fire(colony.pos + direction / 2.0, AntRandom.nextDouble(5.0, 12.0))
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
        ticks++

        for (colony in this.colonies)
        {
            colony.pheromone.evaporate()
            colony.pheromone.makeCache()
        }

        this.ants.forEach(Ant::tick)
        this.ants.removeIf(Ant::dead)

        val numFoodBefore = this.food.size
        this.food.removeIf(Food::isDepleted)
        while (this.food.size < numFoodBefore)
            this.createRandomFood()
    }
}