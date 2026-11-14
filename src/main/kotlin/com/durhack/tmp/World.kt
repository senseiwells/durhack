package com.durhack.tmp

import org.openrndr.math.Vector2

class World {
    val colonies = mutableListOf<Colony>()
    val ants = mutableListOf<Ant>()
    val food = mutableListOf<Food>()

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

    fun createRandomFood() {
        this.createFood(this.colonies.random().pos + Vector2.random(50.0) )
    }

    fun getNearestFood(pos: Vector2): Food {
        return this.food.minBy { food -> food.pos.squaredDistanceTo(pos) }
    }

    fun tick() {
        for (ant in this.ants) {
            ant.tick()
        }

        this.food.removeIf(Food::isDepleted)
        if (this.food.isEmpty()) {
            this.createRandomFood()
        }
    }
}