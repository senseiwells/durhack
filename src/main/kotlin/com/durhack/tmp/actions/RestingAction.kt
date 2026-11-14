package com.durhack.tmp.actions

import com.durhack.tmp.Ant

class RestingAction(
    override val ant: Ant
): Action {
    override fun act(): Action {
        this.ant.tiredness--
        if (this.ant.tiredness < 20) {
            return AdventuringAction(this.ant)
        }
        return this
    }
}