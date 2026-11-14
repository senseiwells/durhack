package com.durhack.tmp.actions

import com.durhack.tmp.Ant

class RestingAction(
    override val ant: Ant
): Action {
    override fun act(): Action {
        this.ant.tiredness -= 1
        if (this.ant.hungriness > 30 && this.ant.tiredness < 20) {
            return AdventuringAction(this.ant)
        }
        return this
    }
}