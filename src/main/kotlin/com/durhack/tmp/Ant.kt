package com.durhack.tmp

import com.durhack.tmp.actions.Action
import com.durhack.tmp.actions.GatheringAction
import com.durhack.tmp.actions.RestingAction
import com.durhack.tmp.actions.ReturningAction
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.presets.HOT_PINK
import org.openrndr.math.Polar
import org.openrndr.math.Vector2
import kotlin.math.max
import kotlin.random.Random

class Ant private constructor(
    val world: World,
    val colony: Colony,
    val traits: Traits,
    var pos: Vector2,
    var dir: Double
) {
    var happiness = 70
    var tiredness = 50
    var hungriness = 0

    var food = 0

    var dead = false
        private set

    var currentAction: Action = RestingAction(this)

    val shouldRender: Boolean
        get() = this.currentAction !is RestingAction

    fun tick() {
        if (this.dead) {
            return
        }
        if (this.world.checkIfImOnFire(this.pos)) {
            this.die()
        }

        if (Random.nextDouble() < 0.2) {
            this.hungriness = max(100, this.hungriness + 1)
        }
        if (this.hungriness <= 0) {
            this.happiness -= 1
        }
        if (this.happiness <= 0) {
            this.die()
        }

        this.currentAction = this.currentAction.act()
    }

    fun move() {
        this.pos += Polar(this.dir, this.traits.speed).cartesian
    }

    fun die() {
        this.dead = true
    }

    fun color(): ColorRGBa {
        return when (this.currentAction) {
            is GatheringAction -> ColorRGBa.GREEN
            is ReturningAction -> ColorRGBa.HOT_PINK
            else -> ColorRGBa.BLUE
        }
    }

    companion object {
        fun create(world: World, colony: Colony, traits: Traits = Traits.random()): Ant {
            return Ant(world, colony, traits, colony.pos, 0.0)
        }
    }
}