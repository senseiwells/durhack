package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.AntRandom

class RestingAction(
    override val ant: Ant
): Action {
    override fun act(): Action {
        if (AntRandom.nextDouble() < 0.3) {
            this.ant.tiredness -= 1
        }

        if (this.ant.tiredness < 20) {
            return AdventuringAction(this.ant)
        }
        return this
    }
}