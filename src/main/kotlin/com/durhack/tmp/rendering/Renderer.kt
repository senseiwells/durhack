package com.durhack.tmp.rendering

import com.durhack.tmp.CACHE_WIDTH
import com.durhack.tmp.DebugSettings
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.ColorBuffer
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.DARK_ORANGE
import org.openrndr.extra.color.presets.SADDLE_BROWN

fun Program.renderWorld(world: WorldRenderState) {
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
        if (ant.visible) {
            drawer.fill = ant.color
            drawer.circle(ant.pos, 2.0)
        }
    }
}

fun Program.renderDebug(buf: ColorBuffer, state: WorldRenderState) {
    for (colony in state.colonies) {
        renderColonyDebug(buf, colony)
    }
}

fun Program.renderColonyDebug(buf: ColorBuffer, state: ColonyRenderState) {
    val cache = state.buffer.latest()
    for (y in 0..<buf.height) {
        for (x in 0..<buf.width) {
            val howSmelly = cache[y * CACHE_WIDTH + x].strength
            buf.shadow[x, y] = ColorRGBa(howSmelly, howSmelly, howSmelly)
        }
    }
    buf.shadow.upload()

    drawer.image(buf, state.bounds)

    // Draw cache outline
    if (DebugSettings.cache) {
        drawer.stroke = ColorRGBa.RED
        drawer.strokeWeight = 1.0
        drawer.fill = ColorRGBa.TRANSPARENT
        drawer.rectangle(state.bounds)
    }

    if (DebugSettings.trails) {
        drawer.strokeWeight = 0.1
        for (trail in state.trails)
            for ((p0, p1) in trail)
                drawer.lineSegment(p0, p1)
    }

    drawer.stroke = ColorRGBa.TRANSPARENT
}