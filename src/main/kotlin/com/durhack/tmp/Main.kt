package com.durhack.tmp

import kotlinx.coroutines.delay
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.ColorBufferShadow
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.DARK_ORANGE
import org.openrndr.extra.color.presets.SADDLE_BROWN
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

fun main() = application {
    configure {
        width = 768
        height = 576
        title = "TheANTS"

        windowResizable = true
    }

    program {
        var world = createWorld()

        var offset = Vector2.ZERO
        var zoom = 1.0
        mouse.dragged.listen { event -> offset += event.dragDisplacement }
        mouse.scrolled.listen { event ->
            val newZoom = clamp(zoom * 1.1.pow(event.rotation.y), 0.2, 10.0)
            val m = event.position
            offset = m - (m - offset) * (newZoom / zoom)
            zoom = newZoom
        }

        keyboard.keyDown.listen { event ->
            if (event.name == "r") {
                world = createWorld()
            }
        }

        // Create pheromone texture
        val pheromoneBuf = colorBuffer(500, 500)
        pheromoneBuf.filter(MinifyingFilter.NEAREST, MagnifyingFilter.NEAREST)

        launch {
            while (true) {
                val tickTime = measureNanoTime {
                    tickWorld(world)
                }.nanoseconds
                if (tickTime < 2.milliseconds) {
                    delay(2.milliseconds - tickTime)
                } else {
                    delay(1.milliseconds)
                }
            }
        }

        extend {
            drawer.translate(offset)
            drawer.scale(zoom)

            renderDebug(pheromoneBuf, world.colonies[0].pheromone)
            renderWorld(world)
        }
    }
}


fun Program.createWorld(): World {
    val world = World()
    val colony = world.createColony(centre)
    repeat(500) {
        world.createAnt(colony)
    }

    // world.createFood(Vector2(224.209016528746, 394.44923405312926))

    world.createRandomFood(30)

    // world.createObstaclesBetweenColoniesAndFood()
    return world
}

fun tickWorld(world: World) {
    world.tick()
}

fun Program.renderWorld(world: World) {
    drawer.drawStyle.colorMatrix = tint(ColorRGBa.WHITE.shade(0.2))

    drawer.fill = ColorRGBa.PINK
    for (colony in world.colonies) {
        drawer.circle(colony.pos, 4.0)
    }

    drawer.fill = ColorRGBa.SADDLE_BROWN
    for (food in world.food) {
        drawer.circle(food.pos, 3.0)
    }

    drawer.fill = ColorRGBa.DARK_ORANGE
    for (obstacle in world.obstacles) {
        drawer.circle(obstacle.pos, obstacle.radius)
    }

    for (ant in world.ants) {
        if (ant.shouldRender) {
            drawer.fill = ant.color()
            drawer.circle(ant.pos, 2.0)
        }
    }
}

fun Program.renderDebug(buf: ColorBuffer, map: PheromoneMap)
{
    for (y in 0..<buf.height)
    {
        for (x in 0..<buf.width)
        {
            val howSmelly = map.cache[y * 500 + x]
            buf.shadow[x, y] = ColorRGBa(howSmelly, howSmelly, howSmelly)
        }
    }
    buf.shadow.upload()
    val cacheBounds = map.cacheBounds
    drawer.image(buf, cacheBounds)

    // Draw cache outline
    drawer.stroke = ColorRGBa.RED
    drawer.strokeWeight = 1.0
    drawer.fill = ColorRGBa.TRANSPARENT
    drawer.rectangle(cacheBounds)

    // Draw trail
    // drawer.strokeWeight = 0.1
    // for (trail in map.trails)
    //     for ((p0, p1) in trail.asSequence().filterIndexed { i, _ -> i % 10 == 0 }.zipWithNext())
    //         drawer.lineSegment(p0.pos, p1.pos)

    drawer.stroke = ColorRGBa.TRANSPARENT
}