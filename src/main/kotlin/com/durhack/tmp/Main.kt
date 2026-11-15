package com.durhack.tmp

import com.durhack.tmp.rendering.initTextures
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
import org.openrndr.extra.color.presets.LAWN_GREEN
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.gui.GUIAppearance
import org.openrndr.math.Vector2
import org.openrndr.math.clamp
import java.util.concurrent.Executors
import kotlin.math.pow
import kotlin.random.Random
import kotlin.system.measureNanoTime
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

private const val GUI_WIDTH = 300

private var offset = Vector2.ZERO
private var zoom = 1.0

private lateinit var world: World

private var tps = DoubleArray(100)
private var fps = DoubleArray(100)

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

        initTextures()
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
    tps[world.ticks % 100] = 1.seconds / tickTime

    if (tickTime < targetTickTime) {
        delay(targetTickTime - tickTime)
    }
}

fun Program.addGui() {
    val gui = GUI(GUIAppearance(barWidth = GUI_WIDTH))
    gui.persistState = false
    gui.compartmentsCollapsedByDefault = false

    gui.add(CoreSettings, "Settings")
    gui.add(DebugSettings, "Debug")
    extend(gui)

    var frames = 0
    var lastSeconds = 0.0
    extend {
        fps[frames++ % 100] = 1 / (seconds - lastSeconds)
        lastSeconds = seconds
    }

    val font = loadFont("data/fonts/default.otf", 24.0)
    extend {
        drawer.fill = ColorRGBa.WHITE
        drawer.fontMap = font

        drawer.text("%.1f FPS".format(fps.average()), GUI_WIDTH + 10.0, 30.0)
        drawer.text("%.1f TPS".format(tps.average()), GUI_WIDTH + 10.0, 58.0)
        drawer.text("Seed $AntRandomSeed", GUI_WIDTH + 10.0, 78.0)
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
            setAntRandom(if (CoreSettings.seed == -1) Random.nextInt() else CoreSettings.seed)
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
    val colony = world.createColony(centre, ColorRGBa.RED)
    repeat(CoreSettings.ants) {
        world.createAnt(colony)
    }

    world.createCasino(centre + Vector2(150.0, 0.0))

    val colony2 = world.createColony(centre + Vector2(300.0, 0.0), ColorRGBa.LAWN_GREEN)
    repeat(CoreSettings.ants) {
        world.createAnt(colony2)
    }

    val colony3 = world.createColony(centre + Vector2(150.0, 150.0), ColorRGBa.YELLOW)
    repeat(CoreSettings.ants) {
        world.createAnt(colony3)
    }

    world.createRandomFood(CoreSettings.food)

    world.createDistributedObstacles()
    return world
}

private fun createSimulationDispatcher(): ExecutorCoroutineDispatcher {
    return Executors.newSingleThreadExecutor { runnable ->
        val thread = Thread(runnable)
        thread.isDaemon = true
        thread
    }.asCoroutineDispatcher()
}