package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.Food
import kotlin.math.floor

class GatheringAction(
    override val ant: Ant,
    val food: Food
): Action {
    override fun act(): Action {
        val carryLimit = floor(this.ant.traits.muscularity * 20).toInt()
        if (this.food.isDepleted || this.ant.food >= carryLimit) {
            return ReturningAction(this.ant)
        }

        this.ant.food += 1
        this.food.remaining -= 1
        return this
    }
}