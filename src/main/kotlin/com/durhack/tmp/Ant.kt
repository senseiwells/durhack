package com.durhack.tmp

import com.durhack.tmp.actions.Action
import com.durhack.tmp.actions.RestingAction
import org.openrndr.math.Vector2

class Ant private constructor(
    private val world: World,
    private val colony: Colony,
    val traits: Traits,
    var pos: Vector2,
    var dir: Double
) {
    var happiness = 70
    var tiredness = 50
    var hungriness = 0

    var currentAction: Action = RestingAction(this)

    val shouldRender: Boolean
        get() = this.currentAction !is RestingAction

    fun act() {
        this.currentAction = this.currentAction.act()
    }

    companion object {
        fun create(world: World, colony: Colony, traits: Traits = Traits.random()): Ant {
            return Ant(world, colony, traits, colony.pos, 0.0)
        }
    }
}