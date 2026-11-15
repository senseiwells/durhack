package com.durhack.tmp

import com.durhack.tmp.createWorld
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import org.openrndr.ExtensionStage
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.draw.colorBuffer
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.DARK_ORANGE
import org.openrndr.extra.color.presets.SADDLE_BROWN
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.GUIAppearance
import org.openrndr.launch
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import java.nio.file.Path
import kotlin.math.pow
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds

private const val GUI_WIDTH = 300

private var offset = Vector2.ZERO
private var zoom = 1.0

lateinit var world: World

fun main() = application {
    configure {
        width = 768
        height = 576
        title = "TheANTS"

        windowResizable = true
    }

    program {
        world = createWorld()

        addGui()
        addListeners()
        recenterShit()

        // Create pheromone texture
        val pheromoneBuf = colorBuffer(500, 500)
        pheromoneBuf.filter(MinifyingFilter.NEAREST, MagnifyingFilter.NEAREST)

        launch {
            while (true) {
                val targetTickTime = (1000.0 / CoreSettings.tps).milliseconds
                val tickTime = measureNanoTime {
                    tickWorld(world)
                }.nanoseconds
                if (tickTime < targetTickTime) {
                    delay(targetTickTime - tickTime)
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

fun Program.addGui() {
    val gui = GUI(GUIAppearance(barWidth = GUI_WIDTH))
    gui.compartmentsCollapsedByDefault = false

    gui.add(CoreSettings, "Settings")
    gui.add(DebugSettings, "Debug")
    extend(gui)
}

fun Program.addListeners() {
    mouse.dragged.listen { event ->
        if (event.position.x > GUI_WIDTH) {
            offset += event.dragDisplacement
        }
    }
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
}

fun Program.recenterShit() {
    var lastWindowSize = window.size
    extend(ExtensionStage.BEFORE_DRAW) {
        val size = window.size
        if (size != lastWindowSize) {
            offset += (size - lastWindowSize) * 0.5
            lastWindowSize = size
        }
    }
}

fun Program.createWorld(): World {
    val world = World()
    val colony = world.createColony(centre)
    repeat(CoreSettings.ants) {
        world.createAnt(colony)
    }

    world.createRandomFood(CoreSettings.food)

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
    if (DebugSettings.cache) {
        drawer.stroke = ColorRGBa.RED
        drawer.strokeWeight = 1.0
        drawer.fill = ColorRGBa.TRANSPARENT
        drawer.rectangle(cacheBounds)
    }

    if (DebugSettings.trails) {
        drawer.strokeWeight = 0.1
        for (trail in map.trails)
            for ((p0, p1) in trail.asSequence().filterIndexed { i, _ -> i % 10 == 0 }.zipWithNext())
                drawer.lineSegment(p0.pos, p1.pos)
    }

    drawer.stroke = ColorRGBa.TRANSPARENT
}