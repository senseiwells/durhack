package com.durhack.tmp

import com.durhack.tmp.actions.RestingAction
import org.openrndr.math.Vector2

class Ant private constructor(
    private val colony: Colony,
    val pos: Vector2,
    val dir: Double
) {
    val currentAction = RestingAction(this)

    val shouldRender: Boolean
        get() = this.currentAction !is RestingAction

    fun act() {
        this.currentAction.act()
    }

    companion object {
        fun create(colony: Colony): Ant {
            return Ant(colony, colony.pos, 0.0)
        }
    }
}