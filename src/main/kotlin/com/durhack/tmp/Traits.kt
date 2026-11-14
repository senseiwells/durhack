package com.durhack.tmp

import kotlin.random.Random

data class Traits(
    val sheepishness: Double,
    val speed: Double,
    val muscularity: Double,
    val selfishness: Double
) {
    companion object {
        fun random(): Traits {
            return Traits(
                Random.nextDouble(),
                Random.nextDouble(),
                Random.nextDouble(),
                Random.nextDouble()
            )
        }
    }
}