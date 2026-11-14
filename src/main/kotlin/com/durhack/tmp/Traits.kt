package com.durhack.tmp

data class Traits(
    val sheepishness: Double,
    val speed: Double,
    val muscularity: Double,
    val selfishness: Double
) {
    companion object {
        fun random(): Traits {
            return Traits(
                AntRandom.nextDouble(),
                AntRandom.nextDouble(0.5, 1.2) * 3,
                AntRandom.nextDouble(),
                AntRandom.nextDouble()
            )
        }
    }
}