package com.durhack.tmp.rendering

import com.durhack.tmp.CachePoint
import org.openrndr.math.Vector2
import org.openrndr.shape.Rectangle
import java.util.concurrent.atomic.AtomicInteger

class TripleBuffer(val size: Int) {
    private val buffers = List(3) { Array(this.size) { CachePoint(-1, 0.0) } }
    private val boundaries = Array(3) { Rectangle(Vector2(0.0, 0.0), 1.0, 1.0) }

    private var writeIndex = 0
    private var readIndex = 1

    private val shared = AtomicInteger(2)

    val writeBuffer: Array<CachePoint> get() = buffers[writeIndex]

    fun publish(boundary: Rectangle) {
        this.boundaries[writeIndex] = boundary
        writeIndex = shared.getAndSet(writeIndex or FRESH) and INDEX_MASK
    }

    fun latest(): Pair<Array<CachePoint>, Rectangle> {
        if (shared.get() and FRESH != 0) {
            readIndex = shared.getAndSet(readIndex) and INDEX_MASK
        }
        return buffers[readIndex] to boundaries[readIndex]
    }

    private companion object {
        const val INDEX_MASK = 0b011
        const val FRESH = 0b100
    }
}