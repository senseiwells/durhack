package com.durhack.tmp

import com.durhack.tmp.rendering.renderDebug
import com.durhack.tmp.rendering.renderWorld
import kotlinx.coroutines.*
import org.openrndr.ExtensionStage
import org.openrndr.Program
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.MagnifyingFilter
import org.openrndr.draw.MinifyingFilter
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.loadFont
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.GUIAppearance
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private const val GUI_WIDTH = 300

private var offset = Vector2.ZERO
private var zoom = 1.0

private lateinit var world: World

private var tps = 0.0


fun main() = application {
    configure {
        width = 768
        height = 576
        title = "TheANTS"

        windowResizable = true
    }

    val simulationDispatcher = createSimulationDispatcher()
    val simulationContext = CoroutineScope(simulationDispatcher + Job() + CoroutineName("Simulation Coroutine"))
    program {
        world = createWorld()

        addGui()
        addListeners()
        recenterShit()

        simulationContext.launch {
            while (true) {
                runSimulationStep()
            }
        }

        // Create pheromone texture
        val buf = colorBuffer(500, 500)
        buf.filter(MinifyingFilter.NEAREST, MagnifyingFilter.NEAREST)
        extend {
            drawer.translate(offset)
            drawer.scale(zoom)

            renderDebug(buf, world.renderState)
            renderWorld(world.renderState)
        }
    }
}

suspend fun runSimulationStep() {
    val targetTickTime = (1000.0 / CoreSettings.tps).milliseconds
    val tickTime = measureNanoTime {
        world.tick()
    }.nanoseconds
    tps = 1.seconds / tickTime

    if (tickTime < targetTickTime) {
        delay(targetTickTime - tickTime)
    }
}

fun Program.addGui() {
    val gui = GUI(GUIAppearance(barWidth = GUI_WIDTH))
    gui.compartmentsCollapsedByDefault = false

    gui.add(CoreSettings, "Settings")
    gui.add(DebugSettings, "Debug")
    extend(gui)

    val font = loadFont("data/fonts/default.otf", 64.0)
    extend {
        drawer.fill = ColorRGBa.RED
        drawer.fontMap = font
        drawer.text("$tps", width - 200.0, 100.0)
    }
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

    val colony2 = world.createColony(centre + Vector2(300.0, 0.0))
    repeat(CoreSettings.ants) {
        world.createAnt(colony2)
    }

    world.createRandomFood(CoreSettings.food)

    // world.createObstaclesBetweenColoniesAndFood()
    return world
}

private fun createSimulationDispatcher(): ExecutorCoroutineDispatcher {
    return Executors.newSingleThreadExecutor { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()
}