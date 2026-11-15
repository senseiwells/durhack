package com.durhack.tmp.actions

import com.durhack.tmp.Ant
import com.durhack.tmp.Casino
import com.durhack.tmp.Food
import kotlin.math.floor

class GamblingAction(
    override val ant: Ant,
    val casino: Casino
): Action {
    override fun act(): Action {
        val success = this.casino.scam(this.ant)
        if (success) {
            return this
        }
        return ReturningAction(this.ant)
    }
}