package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.Food

class GatheringAction(
    override val ant: Ant,
    val food: Food
): Action {
    override fun act(): Action {
        if (this.food.isDepleted) {
            return ReturningAction(this.ant)
        }

        this.ant.food += 1
        this.food.remaining -= 1
        return this
    }
}