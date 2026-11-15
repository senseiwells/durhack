package com.durhack.tmp

import com.durhack.tmp.actions.Action
import com.durhack.tmp.actions.RestingAction
import org.openrndr.color.ColorRGBa
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

class Ant private constructor(
    val world: World,
    val colony: Colony,
    val traits: Traits,
    var pos: Vector2,
    var dir: Double
) {
    val id = ID_COUNTER++

    val adventuringTrail = mutableListOf<Vector2>()

    var tiredness = 50
    var cashmoney = 1000

    var food = 0

    var dead = false
        private set

    var currentAction: Action = RestingAction(this)

    val visible: Boolean
        get() = this.currentAction !is RestingAction

    fun tick() {
        if (this.dead) {
            return
        }
        if (this.world.checkIfImOnFire(this.pos)) {
            this.die()
        }

        this.currentAction = this.currentAction.act()
    }

    fun move(scale: Double = 1.0) {
        this.pos += Polar(this.dir, this.traits.speed * scale).cartesian
    }

    fun die() {
        // this.dead = true
        this.pos = this.colony.pos
        this.currentAction = RestingAction(this)

        this.tiredness = 50
    }

    fun color(): ColorRGBa {
        return this.colony.color
    }

    fun onReturnToColony() {
        this.pos = this.colony.pos
        this.food = 0
        this.adventuringTrail.clear()
    }

    companion object {
        private var ID_COUNTER = 0

        fun create(world: World, colony: Colony, traits: Traits = Traits.random()): Ant {
            return Ant(world, colony, traits, colony.pos, AntRandom.nextDouble() * 360)
        }
    }
}