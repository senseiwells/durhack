package com.durhack.tmp.rendering

import com.durhack.tmp.CACHE_WIDTH
import com.durhack.tmp.DebugSettings
import org.openrndr.Program
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.BlendMode
import org.openrndr.draw.ColorBuffer
import org.openrndr.draw.Drawer
import org.openrndr.draw.colorBuffer
import org.openrndr.draw.isolated
import org.openrndr.draw.loadImage
import org.openrndr.extra.color.colormatrix.tint
import org.openrndr.extra.color.presets.DARK_ORANGE
import org.openrndr.extra.color.presets.SADDLE_BROWN
import org.openrndr.math.Vector2
import org.openrndr.math.asRadians
import org.openrndr.shape.Rectangle

private lateinit var antTex: ColorBuffer
private lateinit var fireTex: ColorBuffer

fun Program.initTextures()
{
    antTex = loadImage("./data/images/ant.png")
    fireTex = loadImage("./data/images/fire.png")
}

fun recolor(src: ColorBuffer, color: ColorRGBa): ColorBuffer
{
    val dst = colorBuffer(src.width, src.height)

    val srcShadow = src.shadow
    srcShadow.download()
    val dstShadow = dst.shadow

    for (y in 0..<src.height)
        for (x in 0..<src.width)
            dstShadow[x, y] = color.copy(alpha = srcShadow[x, y].alpha)

    dstShadow.upload()
    return dst
}

fun Drawer.imageCentered(cb: ColorBuffer, center: Vector2, radius: Double, rotation: Double) {
    isolated {
        translate(center)
        rotate(Math.toDegrees(rotation))
        image(cb, -radius, -radius, radius * 2, radius * 2)
    }
}

fun Program.renderWorld(world: WorldRenderState) {
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
        drawer.imageCentered(fireTex, obstacle.pos, obstacle.radius, 0.0)
    }

    var coloredAnts = mutableMapOf<ColorRGBa, ColorBuffer>()
    for (ant in world.ants) {
        if (ant.visible) {
            val coloredAnt = coloredAnts.getOrPut(ant.color, { recolor(antTex, ant.color) })
            drawer.imageCentered(coloredAnt, ant.pos, 5.0, (ant.dir + 90).asRadians)
        }
    }
}

fun Program.renderDebug(buf: ColorBuffer, state: WorldRenderState) {
    for (colony in state.colonies) {
        renderColonyDebug(buf, colony)
    }
}

fun Program.renderColonyDebug(buf: ColorBuffer, state: ColonyRenderState) {
    val (cache, bounds) = state.buffer.latest()
    for (y in 0..<buf.height) {
        for (x in 0..<buf.width) {
            val howSmelly = cache[y * CACHE_WIDTH + x].strength
            buf.shadow[x, y] = state.color.copy(alpha = howSmelly)
        }
    }
    buf.shadow.upload()

    drawer.drawStyle.blendMode = BlendMode.BLEND
    drawer.image(buf, bounds)

    // Draw cache outline
    if (DebugSettings.cache) {
        drawer.stroke = state.color
        drawer.strokeWeight = 1.0
        drawer.fill = ColorRGBa.TRANSPARENT
        drawer.rectangle(bounds)
    }

    if (DebugSettings.trails) {
        drawer.strokeWeight = 0.1
        drawer.stroke = state.color
        drawer.fill = state.color
        for (trail in state.trails)
            for ((p0, p1) in trail)
                drawer.lineSegment(p0, p1)
    }

    drawer.stroke = ColorRGBa.TRANSPARENT
}