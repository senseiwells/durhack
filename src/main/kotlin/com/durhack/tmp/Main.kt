package com.durhack.tmp

import kotlinx.coroutines.delay
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.SADDLE_BROWN
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    program {
        val world = World()

        initializeWorld(world)

        var offset = Vector2.ZERO
        var zoom = 1.0
        mouse.dragged.listen { event -> offset += event.dragDisplacement }
        mouse.scrolled.listen { event -> zoom = clamp(zoom + event.rotation.y, 0.2, 10.0) }

        launch {
            while (true) {
                val tickTime = measureNanoTime {
                    tickWorld(world)
                }.nanoseconds
                if (tickTime < 50.milliseconds) {
                    delay(50.milliseconds - tickTime)
                }
            }
        }

        extend {
            drawer.translate(offset)
            drawer.translate(centre)
            drawer.scale(zoom)
            drawer.translate(-centre)

            renderWorld(world)
        }
    }
}


fun Program.initializeWorld(world: World) {
    val colony = world.createColony(centre)
    repeat(5) {
        world.createAnt(colony)
    }

    repeat(5) {
        world.createRandomFood()
    }
}

fun tickWorld(world: World) {
    world.tick()
}

fun Program.renderWorld(world: World) {
    drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))

    drawer.fill = ColorRGBa.PINK
    for (colony in world.colonies) {
        drawer.circle(colony.pos, 10.0)
    }

    drawer.fill = ColorRGBa.SADDLE_BROWN
    for (food in world.food) {
        drawer.circle(food.pos, 7.0)
    }

    for (ant in world.ants) {
        if (ant.shouldRender) {
            drawer.fill = ant.color()
            drawer.circle(ant.pos, 5.0)
        }
    }
}