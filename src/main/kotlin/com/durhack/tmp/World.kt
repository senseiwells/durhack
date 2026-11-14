package com.durhack.tmp

import org.openrndr.math.Vector2

class World {
    val colonies = mutableListOf<Colony>()
    val ants = mutableListOf<Ant>()

    fun createColony(pos: Vector2): Colony {
        val colony = Colony(pos)
        this.colonies.add(colony)
        return colony
    }

    fun createAnt(colony: Colony): Ant {
        val ant = Ant.create(colony)
        this.ants.add(ant)
        return ant
    }

    fun tick() {
        for (ant in this.ants) {
            ant.act()
        }
    }
}