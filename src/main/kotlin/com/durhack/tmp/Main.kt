package com.durhack.tmp

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.openrndr.Dispatcher
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.launch
import org.openrndr.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        // val font = loadFont("data/fonts/default.otf", 64.0)

        val world = World()

        val colony = world.createColony(Vector2(width / 2.0, height / 2.0))
        for (i in 0..<5) {
            world.createAnt(colony)
        }


        launch {
            while (true) {
                world.tick()
                delay(1.seconds)
            }
        }

        extend {
            drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))

            drawer.fill = ColorRGBa.PINK
            for (colony in world.colonies) {
                drawer.circle(colony.pos, 10.0)
            }


            drawer.fill = ColorRGBa.BLUE
            for (ant in world.ants) {
                if (ant.shouldRender) {
                    drawer.circle(ant.pos, 5.0)
                }
            }
        }
    }
}
