package com.durhack.tmp

import kotlinx.coroutines.delay
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
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
                }
                delay(50.milliseconds - tickTime.nanoseconds)
            }
        }

        extend {
            drawer.translate(offset)
            drawer.translate(Vector2(width / 2.0, height / 2.0))
            drawer.scale(zoom)
            drawer.translate(Vector2(-width / 2.0, -height / 2.0))
            renderWorld(world)
        }
    }
}


fun Program.initializeWorld(world: World) {
    val colony = world.createColony(Vector2(width / 2.0, height / 2.0))
    repeat(5) {
        world.createAnt(colony)
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


    drawer.fill = ColorRGBa.BLUE
    for (ant in world.ants) {
        if (ant.shouldRender) {
            drawer.circle(ant.pos, 5.0)
        }
    }
}